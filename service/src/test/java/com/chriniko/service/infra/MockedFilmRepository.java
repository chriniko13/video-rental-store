package com.chriniko.service.infra;

import com.chriniko.domain.Film;
import com.chriniko.domain.repository.FilmRepository;
import com.chriniko.domain.FilmType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MockedFilmRepository extends FilmRepository {

	private final ConcurrentHashMap<Long, Film> filmsById = new ConcurrentHashMap<>();
	private final AtomicLong idSequence = new AtomicLong();

	@Override public Film find(Long id) {
		return filmsById.get(id);
	}

	@Override public long saveFilm(String filmName, FilmType filmType) {
		return this.save(Film.create(filmName, filmType));
	}

	@Override public Long save(Film film) {
		long id = idSequence.incrementAndGet();
		film.setId(id);
		filmsById.put(id, film);
		return id;
	}

	@Override public boolean delete(Long id) {
		return filmsById.remove(id) != null;
	}

	@Override public void save(Iterable<Film> films) {
		films.forEach(film -> {
			long id = idSequence.incrementAndGet();
			film.setId(id);

			filmsById.put(id, film);
		});
	}

	@Override public long count() {
		return filmsById.size();
	}
}
