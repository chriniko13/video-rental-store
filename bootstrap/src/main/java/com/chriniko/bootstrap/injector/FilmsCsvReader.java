package com.chriniko.bootstrap.injector;

import com.chriniko.common.infra.InfrastructureException;
import com.google.common.collect.Iterators;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Stream;

public final class FilmsCsvReader extends Thread {

	private static final Logger logger = Logger.getLogger(FilmsCsvReader.class);

	private final String resourceName;
	private final ArrayBlockingQueue<CreateFilmTask> filmsQueue;
	private final int batchSize;
	private final int readLimit;

	FilmsCsvReader(String resourceName, ArrayBlockingQueue<CreateFilmTask> filmsQueue, int batchSize, int readLimit) {
		this.resourceName = resourceName;
		this.filmsQueue = filmsQueue;
		this.batchSize = batchSize;
		this.readLimit = readLimit;
	}

	@Override public void run() {
		final int[] acc = new int[] { 0 }; // Note: use heap in order to mutate inside closure.

		try (
				InputStream is = DataPopulator.class.getResourceAsStream(resourceName);
				Stream<String> lines = new BufferedReader(new InputStreamReader(is)).lines().skip(1).limit(readLimit)
		) {
			Iterators.partition(lines.iterator(), batchSize).forEachRemaining(batch -> {
				CreateFilmTask task = new CreateFilmTask(batch);
				try {
					filmsQueue.put(task);
					acc[0] += batch.size();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new InfrastructureException(e);
				}
			});

		} catch (Exception e) {
			throw new InfrastructureException(e);
		}

		logger.info("finished reading csv and creating tasks, accumulator test: " + acc[0]);
	}

}
