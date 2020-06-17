package com.chriniko.service;

import com.chriniko.domain.base.TransactionRollbackException;
import com.chriniko.service.error.ErrorCodes;
import com.chriniko.service.error.ErrorResolver;
import com.chriniko.service.error.PostgresqlErrCodes;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import org.apache.log4j.Logger;
import org.postgresql.util.PSQLException;

import javax.persistence.OptimisticLockException;

public abstract class ServiceVerticle extends AbstractVerticle {

	protected final Logger logger;

	protected ServiceVerticle(Logger logger) {
		this.logger = logger;
	}

	protected <T> Handler<Message<T>> enrichWithErrorHandling(Handler<Message<T>> h) {
		return msg -> {
			try {
				h.handle(msg);
			} catch (TransactionRollbackException error) {
				logger.error("transaction error occurred: " + error.getMessage(), error);

				final Throwable cause = error.getCause();

				if (cause instanceof OptimisticLockException) {
					msg.fail(ErrorCodes.DB_OPTIMISTIC_LOCK_ERROR, cause.getMessage());
				} else {
					handleDbError(msg, error, cause);
				}

			} catch (Exception unknown) {
				logger.error("unknown error occurred: " + unknown.getMessage(), unknown);
				msg.fail(ErrorCodes.CRITICAL_UNKNOWN_ERROR, unknown.getMessage());
			}
		};
	}

	private <T> void handleDbError(Message<T> msg, TransactionRollbackException error, Throwable cause) {

		PSQLException psqlException = ErrorResolver.getPSQLException(cause);
		if (psqlException != null) {

			String errorMessage = psqlException.getMessage();

			final int errorCode;
			if (psqlException.getSQLState().equals(PostgresqlErrCodes.UNIQUE_VIOLATION)) {
				errorCode = ErrorCodes.PSQL_UNIQUE_VIOLATION_ERROR;
			} else {
				errorCode = ErrorCodes.PSQL_GENERAL_ERROR;
			}

			msg.fail(errorCode, errorMessage);

		} else {
			msg.fail(ErrorCodes.DB_UNKNOWN_ERROR, error.getMessage());
		}
	}

}
