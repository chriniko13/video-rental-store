package com.chriniko.common.infra;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import io.vertx.core.Vertx;

import java.util.Optional;

public abstract class BootstrapComponent {

	public void start(Injector injector, Vertx vertx, boolean waitUntilDeploymentFinishes) {
		registerEventBusCodecs(vertx);
		deployVerticles(injector, vertx, waitUntilDeploymentFinishes);
	}

	protected abstract void deployVerticles(Injector injector, Vertx vertx, boolean waitUntilDeploymentFinishes);

	protected abstract void registerEventBusCodecs(Vertx vertx);

	public abstract Optional<AbstractModule> getComponentModule();
}
