package com.chriniko.service.error;

public class ErrorCodes {

	public static final int PSQL_GENERAL_ERROR = -1;

	public static final int PSQL_UNIQUE_VIOLATION_ERROR = -2;

	// Note: this should never happen, we should map all vendor specific errors, but we handle it in order to identify our miss.
	public static final int DB_UNKNOWN_ERROR = -3;

	public static final int ENTITY_NOT_EXISTS_ERROR = -4;

	public static final int OPERATION_NOT_SUPPORTED_YET_ERROR = -5;

	public static final int DB_OPTIMISTIC_LOCK_ERROR = -6;

	public static final int CRITICAL_UNKNOWN_ERROR = -7;

	private ErrorCodes() {
	}

}
