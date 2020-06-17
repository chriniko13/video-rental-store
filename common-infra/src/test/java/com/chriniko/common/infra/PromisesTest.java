package com.chriniko.common.infra;

import io.vertx.core.Promise;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PromisesTest {

	@Test
	public void merge_success_scenario() {

		// given
		Promise<String> promise1 = Promise.promise();
		Promise<String> promise2 = Promise.promise();
		Promise<String> promise3 = Promise.promise();
		Promise<String> promise4 = Promise.promise();
		Promise<String> promise5 = Promise.promise();

		final Object coordinator = new Object();

		CompletableFuture.runAsync(() -> {
			synchronized (coordinator) {
				try {
					coordinator.wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException(e);
				}
			}
			promise1.complete(this.toString());
		});

		CompletableFuture.runAsync(() -> {
			synchronized (coordinator) {
				try {
					coordinator.wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException(e);
				}
			}
			promise2.complete(this.toString());
		});

		CompletableFuture.runAsync(() -> {
			synchronized (coordinator) {
				try {
					coordinator.wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException(e);
				}
			}
			promise3.complete(this.toString());
		});

		CompletableFuture.runAsync(() -> {
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			}
			promise4.complete(this.toString());
		});

		CompletableFuture.runAsync(() -> {
			try {
				Thread.sleep(2500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			}
			synchronized (coordinator) {
				coordinator.notifyAll();
				;
			}
			promise5.complete(this.toString());
		});

		// when
		AtomicInteger operationsCounter = new AtomicInteger();
		Promise<List<String>> mergedResult = Promises.merge(
				res -> operationsCounter.incrementAndGet(),
				Arrays.asList(promise1, promise2, promise3, promise4, promise5)
		);

		// then
		Promises.waitPromise(mergedResult);
		assertEquals(5, operationsCounter.get());
		assertTrue(mergedResult.future().succeeded());

		List<String> results = mergedResult.future().result();
		assertEquals(5, results.size());

	}

	@Test
	public void merge_failure_scenario() {

		// given
		Promise<String> promise1 = Promise.promise();
		Promise<String> promise2 = Promise.promise();
		Promise<String> promise3 = Promise.promise();

		final Object coordinator = new Object();

		CompletableFuture.runAsync(() -> {
			synchronized (coordinator) {
				try {
					coordinator.wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException(e);
				}
			}
			promise1.complete(this.toString());
		});

		CompletableFuture.runAsync(() -> {
			synchronized (coordinator) {
				try {
					coordinator.wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException(e);
				}
			}
			promise2.complete(this.toString());
		});

		CompletableFuture.runAsync(() -> {
			try {
				Thread.sleep(1200);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			}
			synchronized (coordinator) {
				coordinator.notifyAll();
			}
			promise3.fail(new IOException("connection issue"));
		});

		// when
		AtomicInteger operationsCounter = new AtomicInteger();
		Promise<List<String>> mergedResult = Promises.merge(
				res -> operationsCounter.incrementAndGet(),
				Arrays.asList(promise1, promise2, promise3)
		);

		// then
		try {
			Promises.waitPromise(mergedResult);
			fail();
		} catch (Exception ex) {
			// then
			assertTrue(ex instanceof RuntimeException);

			Throwable cause = ex.getCause();
			assertTrue(cause instanceof ExecutionException);

			ExecutionException ee = (ExecutionException) cause;
			Throwable cause2 = ee.getCause();
			assertTrue(cause2 instanceof IOException);

			assertEquals("connection issue", cause2.getMessage());
		}

	}
}
