package com.chriniko.resource.route;

import com.chriniko.resource.error.BadPathParamProvidedException;
import com.chriniko.resource.error.PathParamIsRequiredException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import javax.annotation.Nullable;

public interface RouteRegistrer {

	void process(Router router, Vertx vertx);

	default @Nullable Long extractPathParam(RoutingContext ctx, String paramName) {
		String id = ctx.request().getParam(paramName);
		if (id == null) {
			ctx.fail(new PathParamIsRequiredException("path parameter is required"));
			return null;
		}

		try {
			return Long.parseLong(id);
		} catch (NumberFormatException ex) {
			ctx.fail(new BadPathParamProvidedException("id provided path parameter is invalid"));
			return null;
		}
	}

	default Handler<AsyncResult<Message<Object>>> replyHandler(RoutingContext ctx, int statusCode) {
		return replyHandler -> {
			if (replyHandler.succeeded()) {

				Object body = replyHandler.result().body();
				String bodyAsJson = Json.encode(body);

				ctx.response()
						.setStatusCode(statusCode)
						.end(bodyAsJson);
			} else {
				Throwable error = replyHandler.cause();
				ctx.fail(error);
			}
		};
	}
}
