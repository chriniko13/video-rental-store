package com.chriniko.resource;

import com.chriniko.resource.error.BadPathParamProvidedException;
import com.chriniko.resource.error.PathParamIsRequiredException;
import com.chriniko.service.error.CommandValidationException;
import com.chriniko.service.error.ErrorCodes;
import com.chriniko.service.protocol.ReplyMessages;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.apache.log4j.Logger;

public class HttpErrorResolverProvider {

	private static final Logger logger = Logger.getLogger(HttpErrorResolverProvider.class);

	private HttpErrorResolverProvider() {
	}

	public static Handler<RoutingContext> getErrorResolver() {

		return ctx -> {
			Throwable cause = ctx.failure();
			if (cause instanceof DecodeException) { // Note: during ser/de of jsons.

				String msg = Json.encode(new ReplyMessages.ServiceOperationFailureMessage("failed to decode, bad payload"));

				ctx.response()
						.setStatusCode(400)
						.end(msg);

			} else if (cause instanceof ReplyException) { // Note: during request-reply on event bus.

				int statusCode = 500;
				String msg = "";

				ReplyException replyException = (ReplyException) cause;
				int failureCode = replyException.failureCode();

				switch (failureCode) {
					case ErrorCodes.PSQL_UNIQUE_VIOLATION_ERROR:
						statusCode = 400;
						msg = "unique constraint violation error occurred, " + replyException.getMessage();
						break;

					case ErrorCodes.OPERATION_NOT_SUPPORTED_YET_ERROR:
						statusCode = 501;
						break;

					case ErrorCodes.ENTITY_NOT_EXISTS_ERROR:
						statusCode = 400;
						msg = replyException.getMessage();
						break;

					case ErrorCodes.DB_OPTIMISTIC_LOCK_ERROR:
						statusCode = 412;
						msg = replyException.getMessage();
						break;

					case ErrorCodes.DB_UNKNOWN_ERROR:
					case ErrorCodes.CRITICAL_UNKNOWN_ERROR:
						logger.error("unknown error occurred, please provide mapping in the future: " + cause, cause);
					case ErrorCodes.PSQL_GENERAL_ERROR:
						statusCode = 500;
						msg = "internal problem with database, try again later or contact support";
						break;
				}

				String encodedFailure = Json.encode(new ReplyMessages.ServiceOperationFailureMessage(msg));

				ctx.response()
						.setStatusCode(statusCode)
						.end(encodedFailure);

			} else if (cause instanceof BadPathParamProvidedException
					|| cause instanceof PathParamIsRequiredException
					|| cause instanceof CommandValidationException) { // Note: during info extraction on resource layer.

				String encodedFailure = Json.encode(new ReplyMessages.ServiceOperationFailureMessage(cause.getMessage()));

				ctx.response()
						.setStatusCode(400)
						.end(encodedFailure);

			} else { // Note: general-unknown error.

				logger.error("unknown error occurred: " + cause, cause);

				String encodedFailure = Json.encode(new ReplyMessages.ServiceOperationFailureMessage(cause.getMessage()));
				ctx.response()
						.setStatusCode(500)
						.end(encodedFailure /* Note: for production deployment, it will be good to not display the error message for security reasons.*/);
			}
		};
	}

}
