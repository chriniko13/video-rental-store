package com.chriniko.bootstrap;

import com.chriniko.bootstrap.injector.DataPopulator;
import com.chriniko.common.infra.BootstrapComponent;
import com.chriniko.common.infra.ModuleList;
import com.chriniko.domain.base.PersistenceProvider;
import com.chriniko.domain.boundary.BootstrapDomainComponent;
import com.chriniko.resource.boundary.BootstrapResourceComponent;
import com.chriniko.service.boundary.BootstrapServiceComponent;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.Vertx;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class Bootstrap {

	private static final Logger logger = Logger.getLogger(Bootstrap.class);

	private Bootstrap() {
	}

	public static void run(boolean injectData) {

		URL logConfigResource = Main.class.getResource("/log4j.xml");
		DOMConfigurator.configure(logConfigResource);

		List<BootstrapComponent> components = Arrays.asList(
				new BootstrapServiceComponent(),  /* service layer */
				new BootstrapResourceComponent(), /* resource layer */
				new BootstrapDomainComponent()    /* domain layer */
		);
		ModuleList modules = new ModuleList(components);

		Injector injector = Guice.createInjector(modules);

		Vertx vertx = Vertx.vertx();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("closing vertx...");
			vertx.close();

			logger.info("closing entity manager provider");
			PersistenceProvider.shutdown();
		}));

		components.forEach(component -> {
			logger.info("firing component: " + component.getClass().getName());
			component.start(injector, vertx, true);
		});

		if (injectData) {
			new DataPopulator().inject(1_000);
		}

		logger.info("platform is up and running....");

	}

	@VisibleForTesting
	public static void runTest() {
		run(false);
	}

}
