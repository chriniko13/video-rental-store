package com.chriniko.resource.boundary;

import com.chriniko.resource.HttpVerticle;
import com.chriniko.common.infra.BootstrapComponent;
import com.chriniko.common.infra.GuiceVertxOperations;
import com.chriniko.common.infra.Promises;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.util.List;
import java.util.Optional;

public class BootstrapResourceComponent extends BootstrapComponent {

	@Override protected void deployVerticles(Injector injector, Vertx vertx, boolean waitUntilDeploymentFinishes) {

		Promise<List<String>> httpServiceVerticleDeployment = deployHttpVericle(vertx, injector);

		if (waitUntilDeploymentFinishes) {
			Promises.waitPromise(httpServiceVerticleDeployment);
		}
	}

	@Override protected void registerEventBusCodecs(Vertx vertx) {
		// Note: not needed.
	}

	@Override public Optional<AbstractModule> getComponentModule() {
		return Optional.empty();
	}

	private Promise<List<String>> deployHttpVericle(Vertx vertx, Injector injector) {
		return GuiceVertxOperations.deploy(vertx,
				injector, HttpVerticle.class, 3
		);
	}

}
