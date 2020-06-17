package com.chriniko.resource;

import com.chriniko.common.infra.ModuleList;
import com.chriniko.resource.boundary.BootstrapResourceComponent;
import com.chriniko.service.boundary.BootstrapServiceComponent;
import com.chriniko.service.command.CreateFilmCommand;
import com.chriniko.service.dto.view.EntityIdViewDto;
import com.chriniko.service.protocol.Events;
import com.chriniko.service.protocol.ReplyMessages;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static com.chriniko.service.protocol.ReplyMessages.ServiceOperationResult.FILM_CREATED;

@RunWith(VertxUnitRunner.class)
public class HttpVerticleTest {

	private Vertx vertx;

	@Before
	public void before(TestContext context) {
		vertx = Vertx.vertx();

		BootstrapResourceComponent bootstrapResourceComponent = new BootstrapResourceComponent();
		ModuleList modules = new ModuleList(Collections.singletonList(bootstrapResourceComponent));

		Injector injector = Guice.createInjector(modules);
		bootstrapResourceComponent.start(injector, vertx, true);

		new BootstrapServiceComponent().registerEventBusCodecs(vertx);

	}

	@After
	public void after(TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}

	@Test
	public void postCreateFilmPayload_worksAsExpected(TestContext context) {

		// given
		WebClient client = WebClient.create(vertx);
		int numberOfPostOperations = 20;
		Async httpPostOperations = context.async(numberOfPostOperations);

		AtomicInteger idSequence = new AtomicInteger();

		vertx.eventBus().consumer(Events.CREATE_FILM, event -> {

			Object body = event.body();
			System.out.println("received message: " + body);

			context.assertTrue(body instanceof CreateFilmCommand);

			CreateFilmCommand dto = (CreateFilmCommand) body;
			context.assertTrue(dto.getName().contains("film"));

			int id = idSequence.incrementAndGet();
			event.reply(new ReplyMessages.ServiceOperationSuccessMessage<>(FILM_CREATED, new EntityIdViewDto(id)));

		});

		for (int i = 0; i < numberOfPostOperations; i++) {

			JsonObject payload = new JsonObject();
			payload.put("name", "film " + i);
			payload.put("type", "NEW_RELEASE");

			System.out.println(payload);

			// when
			String expected = "\\{\"serviceOperationResult\":\"FILM_CREATED\",\"result\":\\{\"view-type\":\"entity-id-view\",\"id\":[0-9]+}}";
			Pattern pattern = Pattern.compile(expected);

			client.postAbs("http://localhost:8080/films").sendJsonObject(payload, ar -> {

				// then
				if (ar.succeeded()) {

					String responseBody = ar.result().bodyAsString();
					int responseStatusCode = ar.result().statusCode();

					context.assertTrue(pattern.asPredicate().test(responseBody));

					context.assertEquals(201, responseStatusCode);
					httpPostOperations.countDown();

				} else {
					context.fail(ar.cause());
					httpPostOperations.complete();
				}

			});

		}

	}

}
