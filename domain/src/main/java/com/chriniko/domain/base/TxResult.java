package com.chriniko.domain.base;

import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Getter
public class TxResult<R> {

	private final boolean error;
	private final Integer errorCode;
	private final String errorMessage;

	private final R result;

	public TxResult(int errCode, String errMsg) {
		error = true;
		errorCode = errCode;
		errorMessage = errMsg;

		result = null;
	}

	public TxResult(R r) {
		error = false;
		errorCode = null;
		errorMessage = null;

		result = r;
	}

	public void execute(BiConsumer<Integer, String> onError, Consumer<R> onSuccess) {
		if (error) {
			onError.accept(errorCode, errorMessage);
		} else {
			onSuccess.accept(result);
		}
	}
}
