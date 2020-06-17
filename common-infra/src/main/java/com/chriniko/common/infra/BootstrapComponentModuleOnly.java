package com.chriniko.common.infra;

import com.google.inject.Injector;
import io.vertx.core.Vertx;

public abstract class BootstrapComponentModuleOnly extends BootstrapComponent {

	@Override protected void deployVerticles(Injector injector, Vertx vertx, boolean waitUntilDeploymentFinishes) {
		// not needed.
	}

	@Override protected void registerEventBusCodecs(Vertx vertx) {
		// note needed.
	}

}
