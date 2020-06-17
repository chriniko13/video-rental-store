package com.chriniko.resource.error;

public class PathParamIsRequiredException extends RuntimeException {

	public PathParamIsRequiredException(String msg) {
		super(msg);
	}
}
