package com.chriniko.resource.route;

import com.chriniko.resource.error.PathParamIsRequiredException;
import com.chriniko.service.command.CreateCustomerCommand;
import com.chriniko.service.command.FindCustomerByIdCommand;
import com.chriniko.service.command.FindCustomerByUsernameCommand;
import com.chriniko.service.command.FindCustomerRentedFilmCopiesCommand;
import com.chriniko.service.command.GetCustomerBonusCommand;
import com.chriniko.service.command.RentFilmCopyCommand;
import com.chriniko.service.command.ReturnFilmCopyCommand;
import com.chriniko.service.dto.RentFilmCopyDto;
import com.chriniko.service.dto.ReturnFilmCopyDto;
import com.chriniko.service.protocol.Events;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;

import java.util.List;

import static com.chriniko.resource.HttpErrorResolverProvider.getErrorResolver;

public class CustomerRouteRegistrer implements RouteRegistrer {

	@Override public void process(Router router, Vertx vertx) {

		getCustomerBonus(router, vertx);

		getCustomerRentedFilmCopies(router, vertx);

		rentFilmCopy(router, vertx);

		returnFilmCopy(router, vertx);

		createCustomer(router, vertx);

		findCustomerById(router, vertx);

		findCustomerByUsername(router, vertx);
	}

	private void findCustomerByUsername(Router router, Vertx vertx) {
		router.get("/search/customers")
				.handler(ctx -> {
					List<String> usernameParams = ctx.queryParam("username");
					if (usernameParams.size() != 1) {
						ctx.fail(new PathParamIsRequiredException("path parameter is required"));
						return;
					}

					String username = usernameParams.get(0);
					FindCustomerByUsernameCommand command = new FindCustomerByUsernameCommand(username, false);
					vertx.eventBus().request(Events.FIND_CUSTOMER_BY_NAME, command, replyHandler(ctx, 200));
				})
				.failureHandler(getErrorResolver());
	}

	private void findCustomerById(Router router, Vertx vertx) {
		router.get("/customers/:id")
				.handler(ctx -> {

					Long customerId = extractPathParam(ctx, "id");
					if (customerId == null)
						return;

					FindCustomerByIdCommand command = new FindCustomerByIdCommand(customerId);
					vertx.eventBus().request(Events.FIND_CUSTOMER_BY_ID, command, replyHandler(ctx, 200));
				})
				.failureHandler(getErrorResolver());
	}

	private void createCustomer(Router router, Vertx vertx) {
		router.post("/customers")
				.handler(ctx -> {
					String payload = ctx.getBodyAsString();
					CreateCustomerCommand command = Json.decodeValue(payload, CreateCustomerCommand.class);
					command.validate();
					vertx.eventBus().request(Events.CREATE_CUSTOMER, command, replyHandler(ctx, 201));
				})
				.failureHandler(getErrorResolver());
	}

	private void returnFilmCopy(Router router, Vertx vertx) {
		router.post("/customer/:id/return")
				.handler(ctx -> {

					Long customerId = extractPathParam(ctx, "id");
					if (customerId == null)
						return;

					String payload = ctx.getBodyAsString();
					ReturnFilmCopyDto dto = Json.decodeValue(payload, ReturnFilmCopyDto.class);

					ReturnFilmCopyCommand command = new ReturnFilmCopyCommand(customerId, dto.getFilmCopyId());
					vertx.eventBus().request(Events.RETURN_FILM_COPY, command, replyHandler(ctx, 200));
				})
				.failureHandler(getErrorResolver());
	}

	private void rentFilmCopy(Router router, Vertx vertx) {
		router.post("/customer/:id/rental")
				.handler(ctx -> {

					Long customerId = extractPathParam(ctx, "id");
					if (customerId == null)
						return;

					String payload = ctx.getBodyAsString();
					RentFilmCopyDto dto = Json.decodeValue(payload, RentFilmCopyDto.class);

					RentFilmCopyCommand command = new RentFilmCopyCommand(customerId, dto.getFilmCopyId());
					vertx.eventBus().request(Events.RENT_FILM_COPY, command, replyHandler(ctx, 201));
				})
				.failureHandler(getErrorResolver());
	}

	private void getCustomerRentedFilmCopies(Router router, Vertx vertx) {
		router.get("/customer/:id/rental/copies")
				.handler(ctx -> {

					Long customerId = extractPathParam(ctx, "id");
					if (customerId == null)
						return;

					boolean returned = false;
					List<String> returnedParams = ctx.queryParam("returned");
					if (!returnedParams.isEmpty()) {
						returned = Boolean.parseBoolean(returnedParams.get(0));
					}

					FindCustomerRentedFilmCopiesCommand command = new FindCustomerRentedFilmCopiesCommand(customerId, returned);
					vertx.eventBus().request(Events.FIND_CUSTOMER_RENTED_FILM_COPIES, command, replyHandler(ctx, 200));
				})
				.failureHandler(getErrorResolver());
	}

	private void getCustomerBonus(Router router, Vertx vertx) {
		router.get("/customer/:id/bonus")
				.handler(ctx -> {

					Long customerId = extractPathParam(ctx, "id");
					if (customerId == null)
						return;

					boolean detailed = false;
					List<String> detailedParams = ctx.queryParam("detailed");
					if (!detailedParams.isEmpty()) {
						detailed = Boolean.parseBoolean(detailedParams.get(0));
					}

					GetCustomerBonusCommand command = new GetCustomerBonusCommand(customerId, detailed);
					vertx.eventBus().request(Events.GET_CUSTOMER_BONUS, command, replyHandler(ctx, 200));
				})
				.failureHandler(getErrorResolver());
	}
}
