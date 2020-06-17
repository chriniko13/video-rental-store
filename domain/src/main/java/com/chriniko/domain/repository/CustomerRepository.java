package com.chriniko.domain.repository;

import com.chriniko.domain.Customer;
import com.chriniko.domain.CustomerRentalHistoryFilmCopy;
import com.chriniko.domain.CustomerRentalHistoryFilmCopyId;
import com.chriniko.domain.FilmCopy;
import com.chriniko.domain.base.BaseRepository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

public class CustomerRepository extends BaseRepository<Customer, Long> {

	@Override protected Class<Customer> getEntityClass() {
		return Customer.class;
	}

	@SuppressWarnings("unchecked")
	public List<CustomerRentalHistoryFilmCopy> getRentalHistories(long customerId, boolean returned) {
		EntityManager em = entityManagerThreadLocal.get();
		String s = returned ? "Customer.getRentalHistories_Returned" : "Customer.getRentalHistories_NotReturned";
		Query q = em.createNamedQuery(s);
		q.setParameter("_id", customerId);
		return q.getResultList();
	}

	public CustomerRentalHistoryFilmCopy rentFilmCopy(FilmCopy filmCopy, Customer c) {
		EntityManager em = entityManagerThreadLocal.get();
		FilmCopy managedFC = em.merge(filmCopy);
		Customer managedC = em.merge(c);
		return managedC.addRentalHistory(managedFC);
	}

	public CustomerRentalHistoryFilmCopy returnFilmCopy(FilmCopy filmCopy, Customer customer) {
		EntityManager em = entityManagerThreadLocal.get();

		FilmCopy managedFC = em.merge(filmCopy);
		Customer managedC = em.merge(customer);

		CustomerRentalHistoryFilmCopy searchResult = em.find(
				CustomerRentalHistoryFilmCopy.class,
				new CustomerRentalHistoryFilmCopyId(managedC.getId(), managedFC.getId())
		);
		if (searchResult == null) {
			throw new IllegalStateException("customer rental history removed during return film operation");
		}
		managedC.returnFilm(searchResult);

		return searchResult;
	}

	public Customer findByUsername(String username) {
		EntityManager em = entityManagerThreadLocal.get();
		TypedQuery<Customer> tq = em.createNamedQuery("Customer.findByUsername", Customer.class);
		tq.setParameter("_username", username);
		return tq.getSingleResult();
	}
}
