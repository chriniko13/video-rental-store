package com.chriniko.common.infra;

import com.google.inject.Injector;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class GuiceVertxOperations {

	private static final Logger logger = Logger.getLogger(GuiceVertxOperations.class);

	private GuiceVertxOperations() {
	}

	/*
		Important Note for verticleInstances int method parameter.

		Due to the fact, that we integrate Guice with Vertx, deploymentOptions#setInstances
		does not work out of the box, so engineer is adviced to pass the prefered number of verticle
		instances as a parameter, so guice injector create a new object every time and deploy it via vertx.

	 */
	public static Promise<List<String> /* deployment ids*/> deploy(
			Vertx vertx,
			Injector injector,
			Class<? extends AbstractVerticle> verticleClazz,
			DeploymentOptions opts,
			int verticleInstances) {

		List<Promise<String>> promises = new ArrayList<>(verticleInstances);

		for (int i = 0; i < verticleInstances; i++) {

			Promise<String> promise = Promise.promise();
			promises.add(promise);

			AbstractVerticle verticleInstance = injector.getInstance(verticleClazz);
			logger.trace("new instance for class: " + verticleClazz + ", memo: " + verticleInstance);

			vertx.deployVerticle(verticleInstance, opts, r -> {
				if (r.succeeded()) {
					promise.complete(r.result() /*deployment id*/);
				} else {
					Throwable error = r.cause();
					promise.fail(error);
				}
			});
		}

		return Promises.merge(
				(String deploymentId) -> logger.trace("deployment success for: " + verticleClazz + ", with deployment id: " + deploymentId),
				promises
		);
	}

	public static Promise<List<String>> deploy(
			Vertx vertx,
			Injector injector,
			Class<? extends AbstractVerticle> verticleClazz,
			int verticleInstances) {
		return deploy(vertx, injector, verticleClazz, new DeploymentOptions(), verticleInstances);
	}

}
