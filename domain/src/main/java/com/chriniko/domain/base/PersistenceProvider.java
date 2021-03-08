package com.chriniko.domain.base;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class PersistenceProvider {

	private static final String PERSISTENCE_UNIT_NAME = "persistence-domain";
	private static final EntityManagerFactory factory;

	static {
		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
	}

	private PersistenceProvider() {
	}

	public static void clearAllTables() {
		EntityManager em = PersistenceProvider.getEntityManagerFactory().createEntityManager();

		em.getTransaction().begin();

		em.createQuery("delete from CustomerRentalHistoryFilmCopy ").executeUpdate();
		em.createQuery("delete from FilmCopy ").executeUpdate();
		em.createQuery("delete from Film ").executeUpdate();
		em.createQuery("delete from Customer ").executeUpdate();

		em.getTransaction().commit();
	}

	public static EntityManagerFactory getEntityManagerFactory() {
		return factory;
	}

	public static void shutdown() {
		if (factory != null) {
			factory.close();
		}
	}

}
