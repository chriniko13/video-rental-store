package com.chriniko.bootstrap.injector;

import com.chriniko.common.infra.ClockProvider;
import com.chriniko.common.infra.InfrastructureException;
import com.chriniko.domain.Customer;
import com.chriniko.domain.FilmCopy;
import com.chriniko.domain.base.PersistenceProvider;
import com.chriniko.domain.base.TxResult;
import com.chriniko.domain.repository.CustomerRepository;
import com.chriniko.domain.repository.FilmCopyRepository;
import com.chriniko.domain.repository.FilmRepository;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/*
	Important Note: No clean code / it is written in fast way, just to populate the database with some records.
 */
public class DataPopulator {

	private static final Logger logger = Logger.getLogger(DataPopulator.class);

	private DataPopulator() {
	}

	// Note: quick test.
	public static void main(String[] args) {
		//inject(10_000);
		//inject(1_000);
		inject(null);
	}

	public static void inject(@Nullable Integer readLimit) {

		// prepare environment for execution...
		PersistenceProvider.clearAllTables();

		String resource = "/movies.csv";

		int totalLines = (int) countLines(resource) - 1 /*for header*/;
		logger.info("file total lines: " + totalLines);

		int noOfWorkers = 80;
		int batchSize = 100;

		if (readLimit == null) {
			readLimit = totalLines;
		}

		ExecutorService executorService = Executors.newFixedThreadPool(noOfWorkers, new ThreadFactoryBuilder().setNameFormat("film-injector-worker-%d").build());

		LongAccumulator totalRecordsPersisted = new LongAccumulator(Long::sum, 0);

		FilmRepository filmRepository = new FilmRepository();
		FilmCopyRepository filmCopyRepository = new FilmCopyRepository();
		CustomerRepository customerRepository = new CustomerRepository();

		final ArrayBlockingQueue<CreateFilmTask> filmsQueue = new ArrayBlockingQueue<>(3500, true);

		Pattern yearExtractor = Pattern.compile("\\([0-9]{4}\\)");
		Pattern titleExtractor = Pattern.compile("\".*\"");

		// start reader...
		long startTime = System.currentTimeMillis();

		FilmsCsvReader filmsCsvReader = new FilmsCsvReader(resource, filmsQueue, batchSize, readLimit);
		filmsCsvReader.setName("films-csv-reader");
		filmsCsvReader.start();

		// start injectors...
		List<FilmsInjectorWorker> injectors = IntStream.rangeClosed(1, noOfWorkers).boxed()
				.map(idx -> new FilmsInjectorWorker(filmsQueue, yearExtractor, titleExtractor, filmRepository, totalRecordsPersisted))
				.collect(Collectors.toList());

		injectors.forEach(executorService::submit);

		// wait for finish...
		while (totalRecordsPersisted.get() < readLimit) {
			logger.info("current injection progress: " + totalRecordsPersisted.get() + " < " + readLimit);
			try {
				Thread.sleep(1000); // Note: pacing.
			} catch (InterruptedException e) {
				// this should not happen, but for just in case.
				Thread.currentThread().interrupt();
			}
		}
		logger.info("finished injection.... totalRecordsPersisted: " + totalRecordsPersisted.get() + " --- readLimit: " + readLimit + " --- actualPersistedRecords: " + filmRepository.count());
		injectors.forEach(Thread::interrupt);

		List<Customer> customers = createCustomers(customerRepository);
		createCustomersInteractionWithFilmCopies(filmCopyRepository, customerRepository, customers);

		long totalTime = System.currentTimeMillis() - startTime;
		logger.info("total time to execute injection in seconds: " + TimeUnit.SECONDS.convert(totalTime, TimeUnit.MILLISECONDS));

		// cleanup...
		executorService.shutdownNow();
	}

	private static List<Customer> createCustomers(CustomerRepository customerRepository) {
		int customerToCreate = 30;
		List<Customer> customers = new ArrayList<>(customerToCreate);
		for (int k = 1; k <= customerToCreate; k++) {
			Customer customer = Customer.create("username_" + k, "firstname_" + k, "initials_" + k, "surname_" + k);
			customers.add(customer);
		}

		customerRepository.getTxBoundary().executeInTx(() -> {
			customerRepository.save(customers);
			return new TxResult<>(null);
		});

		return customers;
	}

	private static void createCustomersInteractionWithFilmCopies(FilmCopyRepository filmCopyRepository,
			CustomerRepository customerRepository, List<Customer> customers) {

		SecureRandom secureRandom = new SecureRandom();

		for (Customer customer : customers) {
			String username = customer.getUsername();

			customerRepository.getTxBoundary().executeInTx(() -> {

				Customer searchResult = customerRepository.findByUsername(username);
				if (searchResult == null)
					throw new IllegalStateException();

				int[] goBackInPast = { 5, 3, 1 };

				int pageSize = 10;
				int totalPages = filmCopyRepository.getTotalPages(pageSize);

				// select a random film copies page...
				int page = secureRandom.nextInt(totalPages) + 1;
				List<FilmCopy> filmCopies = filmCopyRepository.get(page, pageSize);

				for (FilmCopy filmCopy : filmCopies) {

					int idx = secureRandom.nextInt(goBackInPast.length);
					int backInPastDays = goBackInPast[idx];

					// go back in time...
					Instant instant = Instant.now().minus(backInPastDays, ChronoUnit.DAYS);
					Clock clock = Clock.fixed(
							instant,
							ZoneOffset.UTC);
					ClockProvider.setClock(clock);

					customerRepository.rentFilmCopy(filmCopy, searchResult);

					// return to now...
					ClockProvider.setClock(Clock.systemDefaultZone());

					customerRepository.returnFilmCopy(filmCopy, searchResult);
				}

				return new TxResult<>(null);
			});

		}
	}

	private static long countLines(String resourceName) {
		try {
			Path path = Paths.get(DataPopulator.class.getResource(resourceName).toURI());
			try (Stream<String> lines = Files.lines(path)) {
				return lines.count();
			}
		} catch (Exception e) {
			throw new InfrastructureException(e);
		}
	}

}
