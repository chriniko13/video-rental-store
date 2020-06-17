package com.chriniko.service.boundary;

import com.chriniko.common.infra.BootstrapComponent;
import com.chriniko.common.infra.GenericCodec;
import com.chriniko.common.infra.GuiceVertxOperations;
import com.chriniko.common.infra.Promises;
import com.chriniko.service.CustomerServiceVerticle;
import com.chriniko.service.FilmServiceVerticle;
import com.chriniko.service.protocol.ReplyMessages;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class BootstrapServiceComponent extends BootstrapComponent {

	private static final String COMMAND_PACKAGE = "com.chriniko.service.command";

	private static final Logger logger = Logger.getLogger(BootstrapServiceComponent.class);

	@Override
	protected void deployVerticles(Injector injector, Vertx vertx, boolean waitUntilDeploymentFinishes) {

		Promise<List<String>> filmServiceVerticleDeployment = deployFilmServiceVerticle(vertx, injector);
		Promise<List<String>> customerServiceVerticleDeployment = deployCustomerServiceVerticle(vertx, injector);

		if (waitUntilDeploymentFinishes) {
			Promises.waitPromise(filmServiceVerticleDeployment);
			Promises.waitPromise(customerServiceVerticleDeployment);
		}
	}

	@Override public void registerEventBusCodecs(Vertx vertx) {

		vertx.eventBus().registerDefaultCodec(
				ReplyMessages.ServiceOperationSuccessMessage.class,
				new GenericCodec<>(ReplyMessages.ServiceOperationSuccessMessage.class)
		);

		// Note: register codecs for commands
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
				.setUrls(ClasspathHelper.forJavaClassPath())
				.filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(COMMAND_PACKAGE))));

		Set<Class<?>> allClasses = reflections.getSubTypesOf(Object.class);
		logger.debug("allClasses loaded from command package in order to register codecs: " + allClasses);

		for (Class<?> clazz : allClasses) {
			@SuppressWarnings("rawtypes")
			GenericCodec genericCodec = new GenericCodec(clazz);
			vertx.eventBus().registerDefaultCodec(clazz, genericCodec);
		}
	}

	@Override public Optional<AbstractModule> getComponentModule() {
		return Optional.empty();
	}

	private Promise<List<String>> deployFilmServiceVerticle(Vertx vertx, Injector injector) {
		DeploymentOptions filmServiceVerticleDeploymentOptions = new DeploymentOptions()
				.setWorker(true)
				.setWorkerPoolName("film-service")
				.setWorkerPoolSize(20)
				.setMaxWorkerExecuteTimeUnit(TimeUnit.MILLISECONDS)
				.setMaxWorkerExecuteTime(3000);

		return GuiceVertxOperations.deploy(vertx,
				injector, FilmServiceVerticle.class,
				filmServiceVerticleDeploymentOptions, 5
		);
	}

	private Promise<List<String>> deployCustomerServiceVerticle(Vertx vertx, Injector injector) {
		DeploymentOptions customerServiceVerticleDeploymentOptions = new DeploymentOptions()
				.setWorker(true)
				.setWorkerPoolName("customer-service")
				.setWorkerPoolSize(20)
				.setMaxWorkerExecuteTimeUnit(TimeUnit.MILLISECONDS)
				.setMaxWorkerExecuteTime(3000);

		return GuiceVertxOperations.deploy(vertx,
				injector, CustomerServiceVerticle.class,
				customerServiceVerticleDeploymentOptions, 5
		);
	}

}
