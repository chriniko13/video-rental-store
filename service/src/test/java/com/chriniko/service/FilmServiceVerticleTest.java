package com.chriniko.service;

import com.chriniko.service.boundary.BootstrapServiceComponent;
import com.chriniko.service.command.CreateFilmCommand;
import com.chriniko.service.infra.BootstrapMockedDomainComponent;
import com.chriniko.service.infra.MockedFilmRepository;
import com.chriniko.service.protocol.Events;
import com.chriniko.service.protocol.ReplyMessages;
import com.chriniko.common.infra.ModuleList;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@RunWith(VertxUnitRunner.class)
public class FilmServiceVerticleTest {

	private Vertx vertx;

	@Inject
	private MockedFilmRepository mockedFilmRepository;

	@Before
	public void before(TestContext context) {

		BootstrapServiceComponent bootstrapServiceComponent = new BootstrapServiceComponent();
		ModuleList modules = new ModuleList(Arrays.asList(bootstrapServiceComponent, new BootstrapMockedDomainComponent()));

		vertx = Vertx.vertx();
		Injector injector = Guice.createInjector(modules);

		bootstrapServiceComponent.start(injector, vertx, true);

		injector.injectMembers(this);
	}

	@After
	public void after(TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}

	@Test
	public void createFilmEvent_worksAsExpected(TestContext context) throws Exception {

		// given
		int createFilmTimes = 50;
		Async firstWriteThenReadAsync = context.async(createFilmTimes);

		for (int i = 0; i < createFilmTimes; i++) {

			// when
			vertx.eventBus().request(Events.CREATE_FILM, new CreateFilmCommand("f" + i, "NEW_RELEASE"), replyMsg -> {

				// then
				if (replyMsg.succeeded()) {
					Message<Object> message = replyMsg.result();
					Object msgBody = message.body();
					System.out.println(Thread.currentThread().getName() + " --- reply received: " + msgBody);

					context.assertTrue(msgBody instanceof ReplyMessages.ServiceOperationSuccessMessage);

					ReplyMessages.ServiceOperationSuccessMessage msg = (ReplyMessages.ServiceOperationSuccessMessage) msgBody;
					context.assertEquals(ReplyMessages.ServiceOperationResult.FILM_CREATED, msg.getServiceOperationResult());

					firstWriteThenReadAsync.countDown();

				} else {
					Throwable error = replyMsg.cause();
					System.out.println(Thread.currentThread().getName() + " --- reply ERROR received: " + error);
					context.fail(error.getMessage());
					firstWriteThenReadAsync.complete();
				}

			});

		}

		// given
		Async readFilmsCountAsync = context.async();

		// when
		firstWriteThenReadAsync.await();

		CompletableFuture.runAsync(() -> {

			// then
			int totalFilmsSaved = (int) mockedFilmRepository.count();
			context.assertEquals(createFilmTimes, totalFilmsSaved);

			readFilmsCountAsync.complete();
		});

	}

}
