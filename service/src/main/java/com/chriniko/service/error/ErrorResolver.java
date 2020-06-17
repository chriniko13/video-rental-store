package com.chriniko.service.error;

import org.postgresql.util.PSQLException;

import javax.annotation.Nullable;

public class ErrorResolver {

	private ErrorResolver() {
	}

	public static @Nullable PSQLException getPSQLException(Throwable error) {
		Throwable current = error.getCause();

		// traverse
		while (current != null && !(current instanceof PSQLException)) {
			current = current.getCause();
		}

		if (current != null) {
			return (PSQLException) current;
		}

		return null;
	}
}
