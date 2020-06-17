package com.chriniko.service;

import com.chriniko.domain.Film;
import com.chriniko.domain.FilmCopy;
import com.chriniko.domain.FilmCopyStatus;
import com.chriniko.domain.FilmType;
import com.chriniko.domain.base.TxResult;
import com.chriniko.domain.repository.FilmCopyRepository;
import com.chriniko.domain.repository.FilmRepository;
import com.chriniko.service.command.CreateFilmCommand;
import com.chriniko.service.command.CreateFilmCopyCommand;
import com.chriniko.service.command.FindFilmByIdCommand;
import com.chriniko.service.command.FindFilmCopyByIdCommand;
import com.chriniko.service.dto.view.EntityIdViewDto;
import com.chriniko.service.dto.view.FilmCopyViewDto;
import com.chriniko.service.dto.view.FilmViewDto;
import com.chriniko.service.error.ErrorCodes;
import com.chriniko.service.protocol.Events;
import com.chriniko.service.protocol.ReplyMessages.ServiceOperationSuccessMessage;
import com.google.inject.Inject;
import io.vertx.core.Promise;
import org.apache.log4j.Logger;

import java.util.function.Function;

import static com.chriniko.service.protocol.ReplyMessages.ServiceOperationResult.*;


public class FilmServiceVerticle extends ServiceVerticle {

	private static final Logger LOG = Logger.getLogger(FilmServiceVerticle.class);

	private final FilmRepository filmRepository;
	private final FilmCopyRepository filmCopyRepository;

	private final Function<Long, String> errMsgFunction;

	@Inject
	public FilmServiceVerticle(FilmRepository filmRepository, FilmCopyRepository filmCopyRepository) {
		super(LOG);
		this.filmRepository = filmRepository;
		this.filmCopyRepository = filmCopyRepository;

		this.errMsgFunction = id -> "film with id: " + id + " not exists";
	}

	@Override
	public void start(Promise<Void> startPromise) {
		handleCreateFilmEvent();
		handleCreateFilmCopyEvent();

		handleFindFilmByIdEvent();
		handleFindFilmCopyByIdEvent();

		startPromise.complete();
	}

	private void handleFindFilmCopyByIdEvent() {
		vertx.eventBus().<FindFilmCopyByIdCommand>consumer(Events.FIND_FILM_COPY_BY_ID, enrichWithErrorHandling(msg -> {

			FindFilmCopyByIdCommand command = msg.body();
			long id = command.getId();

			TxResult<FilmCopy> txResult = filmRepository.getTxBoundary().executeInTx(() -> {
				FilmCopy result = filmCopyRepository.find(id);
				if (result == null) {
					return new TxResult<>(ErrorCodes.ENTITY_NOT_EXISTS_ERROR, errMsgFunction.apply(id));
				} else {
					return new TxResult<>(result);
				}
			});

			txResult.execute(
					msg::fail,
					r -> msg.reply(
							new ServiceOperationSuccessMessage<>(
									FIND_FILM_COPY_BY_ID_EXECUTED,
									new FilmCopyViewDto(r.getId(), r.getReference(), r.getStatus().name())
							)
					)
			);

		}));
	}

	private void handleCreateFilmEvent() {
		vertx.eventBus().<CreateFilmCommand>consumer(Events.CREATE_FILM, enrichWithErrorHandling(msg -> {
			CreateFilmCommand command = msg.body();
			FilmType filmType = FilmType.valueOf(command.getType());

			TxResult<Long> txResult = filmRepository.getTxBoundary().executeInTx(() -> {
				long id = filmRepository.saveFilm(command.getName(), filmType);
				return new TxResult<>(id);
			});

			Long id = txResult.getResult();
			msg.reply(new ServiceOperationSuccessMessage<>(FILM_CREATED, new EntityIdViewDto(id)));

		}));
	}

	private void handleCreateFilmCopyEvent() {
		vertx.eventBus().<CreateFilmCopyCommand>consumer(Events.CREATE_FILM_COPY, enrichWithErrorHandling(msg -> {

			CreateFilmCopyCommand command = msg.body();
			long filmId = command.getFilmId();

			TxResult<EntityIdViewDto> txResult = filmRepository.getTxBoundary().executeInTx(() -> {

				Film film = filmRepository.find(filmId);
				if (film == null) {
					return new TxResult<>(ErrorCodes.ENTITY_NOT_EXISTS_ERROR, errMsgFunction.apply(filmId));
				}

				String copyStatus = command.getCopyStatus();
				FilmCopyStatus status = FilmCopyStatus.valueOf(copyStatus);

				FilmCopy filmCopy = filmRepository.createCopy(film, status);
				long id = filmCopy.getId();

				return new TxResult<>(new EntityIdViewDto(id));

			});

			txResult.execute(
					msg::fail,
					r -> {
						ServiceOperationSuccessMessage<EntityIdViewDto> m = new ServiceOperationSuccessMessage<>(FILM_COPY_CREATED, txResult.getResult());
						msg.reply(m);
					}
			);
		}));
	}

	private void handleFindFilmByIdEvent() {
		vertx.eventBus().<FindFilmByIdCommand>consumer(Events.FIND_FILM_BY_ID, enrichWithErrorHandling(msg -> {

			FindFilmByIdCommand command = msg.body();
			long id = command.getId();

			TxResult<Film> txResult = filmRepository.getTxBoundary().executeInTx(() -> {

				Film film = filmRepository.find(id);
				if (film == null) {
					return new TxResult<>(ErrorCodes.ENTITY_NOT_EXISTS_ERROR, errMsgFunction.apply(id));
				}
				return new TxResult<>(film);

			});

			txResult.execute(
					msg::fail,
					r -> msg.reply(
							new ServiceOperationSuccessMessage<>(
									FIND_FILM_BY_ID_EXECUTED,
									new FilmViewDto(r.getId(), r.getName(), r.getFilmType().name())
							)
					));

		}));
	}

}
