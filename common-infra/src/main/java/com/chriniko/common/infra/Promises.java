package com.chriniko.common.infra;

import io.vertx.core.Promise;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class Promises {

	private static final int LATCH_TIMEOUT_IN_SECONDS = 10;

	private static final int WAIT_PROMISE_TIMEOUT_IN_SECONDS = 5;

	private Promises() {
	}

	// Note: List<Promise<T>> ---TO---> Promise<List<T>>
	public static <I> Promise<List<I>> merge(Consumer<I> resultConsumer, List<Promise<I>> promises) {

		final Promise<List<I>> result = Promise.promise();
		final Queue<I> accumulator = new ConcurrentLinkedQueue<>();
		final CountDownLatch latch = new CountDownLatch(promises.size());

		promises.forEach(p ->
				p.future().onComplete(r -> {
					if (r.failed()) {
						result.fail(r.cause()); // Note: fail-fast approach.
					} else {
						I i = r.result();
						if (resultConsumer != null) {
							resultConsumer.accept(i);
						}
						accumulator.add(i);
						latch.countDown();
					}
				})
		);

		CompletableFuture.runAsync(() -> {
			boolean reachedZero;
			try {
				reachedZero = latch.await(LATCH_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new InfrastructureException(e);
			}
			if (!reachedZero) {
				throw new InfrastructureException(new TimeoutException("promises execution took too long"));
			}
			result.complete(new ArrayList<>(accumulator));
		});

		return result;
	}

	public static void waitPromise(Promise<?> promise) {
		try {
			promise
					.future()
					.toCompletionStage()
					.toCompletableFuture().get(WAIT_PROMISE_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new InfrastructureException(e);
		} catch (TimeoutException | ExecutionException e) {
			throw new InfrastructureException(e);
		}
	}

}
