package com.chriniko.service.error;

public class CommandValidationException extends RuntimeException {

	public CommandValidationException(String msg) {
		super(msg);
	}

}
