package com.chriniko.domain.base;

@FunctionalInterface
public interface TxOperation<R> {

	R process();
}
