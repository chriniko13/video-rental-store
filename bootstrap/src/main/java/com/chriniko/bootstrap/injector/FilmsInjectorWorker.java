package com.chriniko.bootstrap.injector;

import com.chriniko.domain.Film;
import com.chriniko.domain.FilmCopyStatus;
import com.chriniko.domain.FilmType;
import com.chriniko.domain.base.TransactionRollbackException;
import com.chriniko.domain.base.TxResult;
import com.chriniko.domain.repository.FilmRepository;
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilmsInjectorWorker extends Thread {

	private static final Logger logger = Logger.getLogger(FilmsInjectorWorker.class);

	private final ArrayBlockingQueue<CreateFilmTask> filmsQueue;
	private final Pattern yearExtractor;
	private final Pattern titleExtractor;
	private final FilmRepository filmRepository;
	private final LongAccumulator totalRecordsPersisted;

	FilmsInjectorWorker(ArrayBlockingQueue<CreateFilmTask> filmsQueue, Pattern yearExtractor,
			Pattern titleExtractor, FilmRepository filmRepository,
			LongAccumulator totalRecordsPersisted) {
		this.filmsQueue = filmsQueue;
		this.yearExtractor = yearExtractor;
		this.titleExtractor = titleExtractor;
		this.filmRepository = filmRepository;
		this.totalRecordsPersisted = totalRecordsPersisted;
	}

	@Override public void run() {

		while (true) {
			final CreateFilmTask task;
			try {
				task = filmsQueue.take();
			} catch (InterruptedException e) {
				logger.trace("injector terminating...");
				Thread.currentThread().interrupt();
				break;
			}

			try {
				process(task);
			} catch (Exception e) {
				logger.error("error occurred during injection, msg: " + e.getMessage(), e);
			}
		}
	}

	private void process(CreateFilmTask task) {
		final List<String> lines = task.getLines();
		final Set<Film> batch = getBatchOfFilms(lines);
		saveFilmsBatch(batch);

		// mark that finished work...
		totalRecordsPersisted.accumulate(lines.size());
		logger.trace("injector finished creating films");
	}

	private Set<Film> getBatchOfFilms(List<String> lines) {

		final Set<Film> batch = new LinkedHashSet<>();

		for (String line : lines) {

			Matcher matcher = yearExtractor.matcher(line);

			boolean couldExtractYear = true;
			if (!matcher.find()) {
				couldExtractYear = false;
			}

			final FilmType filmType;
			if (couldExtractYear) {
				int start = matcher.start() + 1;
				int end = matcher.end() - 1;
				int year = Integer.parseInt(line.substring(start, end));
				if (year < 2000) {
					filmType = FilmType.OLD;
				} else if (year < 2015) {
					filmType = FilmType.REGULAR;
				} else {
					filmType = FilmType.NEW_RELEASE;
				}
			} else {
				filmType = FilmType.OLD;
			}

			String[] data = line.split(",");

			final String title;
			if (data[1].startsWith("\"")) {

				Matcher titleMatcher = titleExtractor.matcher(line);
				if (!titleMatcher.find()) {
					logger.warn("could not extract title from \" (double quotes) skipping...");
					continue;
				}

				title = titleMatcher.group().replace("\"", "");
			} else {
				title = data[1];
			}

			batch.add(Film.create(title, filmType));
		}
		return batch;
	}

	private void saveFilmsBatch(Set<Film> batch) {
		try {
			filmRepository.getTxBoundary().executeInTx(() -> {

				// Note: due to duplicate records in csv file, we make an effort here to save some batches.
				Iterator<Film> iterator = batch.iterator();
				while (iterator.hasNext()) {
					Film f = iterator.next();
					Film searchResult = filmRepository.findByName(f.getName());
					if (searchResult != null)
						iterator.remove();
				}

				filmRepository.save(batch);

				// now create some film copies
				for (Film film : batch) {
					String filmName = film.getName();
					Film searchResult = filmRepository.findByName(filmName);
					if (searchResult == null)
						continue;

					for (FilmCopyStatus value : FilmCopyStatus.values()) {
						filmRepository.createCopy(searchResult, value);
					}

				}

				return new TxResult<>(null);
			});
		} catch (TransactionRollbackException e) {

			Throwable current = e.getCause();
			boolean constraintProblem = false;

			while (current != null) {
				if (current instanceof ConstraintViolationException) {
					constraintProblem = true;
					break;
				}
				current = current.getCause();
			}

			if (!constraintProblem) {
				throw e;
			}
		}
	}

}
