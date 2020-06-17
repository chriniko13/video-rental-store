package com.chriniko.resource.route;

import com.chriniko.service.command.CreateFilmCommand;
import com.chriniko.service.command.CreateFilmCopyCommand;
import com.chriniko.service.command.FindFilmByIdCommand;
import com.chriniko.service.command.FindFilmCopyByIdCommand;
import com.chriniko.service.dto.CreateFilmCopyDto;
import com.chriniko.service.protocol.Events;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;

import static com.chriniko.resource.HttpErrorResolverProvider.getErrorResolver;

public class FilmRouteRegistrer implements RouteRegistrer {

	@Override public void process(Router router, Vertx vertx) {

		getFilmById(router, vertx);

		createFilm(router, vertx);

		createFilmCopy(router, vertx);

		getFilmCopyById(router, vertx);
	}

	private void getFilmCopyById(Router router, Vertx vertx) {
		router.get("/copies/:id")
				.handler(ctx -> {
					Long copyId = extractPathParam(ctx, "id");
					if (copyId == null)
						return;

					FindFilmCopyByIdCommand command = new FindFilmCopyByIdCommand(copyId);
					vertx.eventBus().request(Events.FIND_FILM_COPY_BY_ID, command, replyHandler(ctx, 200));
				})
				.failureHandler(getErrorResolver());
	}

	private void createFilmCopy(Router router, Vertx vertx) {
		router.post("/films/:filmId/copies/")
				.handler(ctx -> {
					Long filmId = extractPathParam(ctx, "filmId");
					if (filmId == null)
						return;

					String payload = ctx.getBodyAsString();
					CreateFilmCopyDto dto = Json.decodeValue(payload, CreateFilmCopyDto.class);

					CreateFilmCopyCommand command = new CreateFilmCopyCommand(filmId, dto.getStatus());
					command.validate();

					vertx.eventBus().request(Events.CREATE_FILM_COPY, command, replyHandler(ctx, 201));
				})
				.failureHandler(getErrorResolver());
	}

	private void createFilm(Router router, Vertx vertx) {
		router.post("/films")
				.handler(ctx -> {
					String payload = ctx.getBodyAsString();
					CreateFilmCommand command = Json.decodeValue(payload, CreateFilmCommand.class);
					command.validate();
					vertx.eventBus().request(Events.CREATE_FILM, command, replyHandler(ctx, 201));
				}).failureHandler(getErrorResolver());
	}

	private void getFilmById(Router router, Vertx vertx) {
		router.get("/films/:id")
				.handler(ctx -> {
					Long filmId = extractPathParam(ctx, "id");
					if (filmId == null)
						return;

					FindFilmByIdCommand command = new FindFilmByIdCommand(filmId);
					vertx.eventBus().request(Events.FIND_FILM_BY_ID, command, replyHandler(ctx, 200));
				})
				.failureHandler(getErrorResolver());
	}
}
