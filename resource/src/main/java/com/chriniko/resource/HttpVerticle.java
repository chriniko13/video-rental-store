package com.chriniko.resource;

import com.chriniko.resource.route.CustomerRouteRegistrer;
import com.chriniko.resource.route.FilmRouteRegistrer;
import com.chriniko.resource.route.RouteRegistrer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class HttpVerticle extends AbstractVerticle {

	@Override
	public void start(Promise<Void> startPromise) {

		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());

		registerRoutes(router);

		vertx.createHttpServer()
				.requestHandler(router)
				.listen(8080, result -> {
					if (result.succeeded()) {
						startPromise.complete();
					} else {
						startPromise.fail(result.cause());
					}
				});
	}

	private void registerRoutes(Router router) {
		RouteRegistrer[] routeRegistrers = new RouteRegistrer[] { new CustomerRouteRegistrer(), new FilmRouteRegistrer() };
		for (RouteRegistrer routeRegistrer : routeRegistrers) {
			routeRegistrer.process(router, vertx);
		}
	}

}
