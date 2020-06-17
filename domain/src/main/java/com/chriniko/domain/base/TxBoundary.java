package com.chriniko.domain.base;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class TxBoundary {

	private final ThreadLocal<EntityManager> emThreadScoped;

	public TxBoundary(ThreadLocal<EntityManager> em) {
		this.emThreadScoped = em;
	}

	public <RESULT extends TxResult<?>> RESULT executeInTx(TxOperation<RESULT> op) {
		EntityManager em = emThreadScoped.get();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			RESULT result = op.process();
			tx.commit();
			return result;
		} catch (Exception ex) {
			tx.rollback();
			throw new TransactionRollbackException(ex);
		} finally {
			closeEntityManager();
		}
	}

	public void closeEntityManager() {
		emThreadScoped.get().close();
		emThreadScoped.remove();
	}
}
