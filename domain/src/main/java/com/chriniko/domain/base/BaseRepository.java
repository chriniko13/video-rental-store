package com.chriniko.domain.base;

import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public abstract class BaseRepository<E extends Record<I>, I> implements Repository<E, I> {

	protected final ThreadLocal<EntityManager> entityManagerThreadLocal;
	private final Logger logger = Logger.getLogger(getEntityClass());

	public BaseRepository() {
		entityManagerThreadLocal = ThreadLocal.withInitial(() -> {
			EntityManager em = PersistenceProvider.getEntityManagerFactory().createEntityManager();

			logger.trace("creating new entity manager for thread: " + Thread.currentThread().getName()
					+ ", identity EntityManager hash: " + System.identityHashCode(em));
			return em;
		});
	}

	protected abstract Class<E> getEntityClass();

	@Override
	public E find(I id) {
		EntityManager em = entityManagerThreadLocal.get();
		return em.find(getEntityClass(), id);
	}

	@Override
	public I save(E entity) {
		EntityManager em = entityManagerThreadLocal.get();
		em.persist(entity);
		return entity.extractId();
	}

	@Override
	public boolean delete(I id) {
		EntityManager em = entityManagerThreadLocal.get();
		E record = em.find(getEntityClass(), id);
		if (record != null) {
			em.remove(record);
			return true;
		}
		return false;
	}

	@Override
	public void save(Iterable<E> entities) {
		EntityManager em = entityManagerThreadLocal.get();
		entities.forEach(em::persist);
	}

	@Override
	public long count() {
		EntityManager em = entityManagerThreadLocal.get();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		query.select(cb.count(query.from(getEntityClass())));
		return em.createQuery(query).getSingleResult();
	}

	@Override
	public int getTotalPages(int pageSize) {
		long totalRecords = this.count();
		return (int) Math.ceil((double) totalRecords / pageSize);
	}

	/**
	 * Returns the records based on provided page and size (pagination).
	 * @param page The page starts from 1.
	 * @param pageSize The size of records which each page has.
	 * @return list of records based on provided page and pageSize.
	 */
	@Override
	public List<E> get(int page, int pageSize) {
		EntityManager em = entityManagerThreadLocal.get();

		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(getEntityClass());
		Root<E> from = criteriaQuery.from(getEntityClass());
		CriteriaQuery<E> select = criteriaQuery.select(from);

		TypedQuery<E> typedQuery = em.createQuery(select);
		typedQuery.setFirstResult(page - 1);
		typedQuery.setMaxResults(pageSize);
		return typedQuery.getResultList();
	}

	@Override public TxBoundary getTxBoundary() {
		return new TxBoundary(entityManagerThreadLocal);
	}
}
