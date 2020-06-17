package com.chriniko.domain.base;

public class TransactionRollbackException extends RuntimeException {

	public TransactionRollbackException(Throwable e) {
		super(e);
	}

	public TransactionRollbackException(String msg) {
		super(msg);
	}
}
