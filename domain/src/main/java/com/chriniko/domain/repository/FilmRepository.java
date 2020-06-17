package com.chriniko.domain.repository;

import com.chriniko.domain.Film;
import com.chriniko.domain.FilmCopy;
import com.chriniko.domain.FilmCopyStatus;
import com.chriniko.domain.FilmType;
import com.chriniko.domain.base.BaseRepository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.Set;

public class FilmRepository extends BaseRepository<Film, Long> {

	@Override protected Class<Film> getEntityClass() {
		return Film.class;
	}

	public FilmCopy createCopy(Film film, FilmCopyStatus status) {
		EntityManager em = entityManagerThreadLocal.get();

		Long filmId = film.getId();
		int copiesSize = film.getCopies().size();

		String reference = filmId + "_" + (copiesSize + 1);

		FilmCopy filmCopy = FilmCopy.create(reference, status);
		em.persist(filmCopy);

		film.addCopy(filmCopy);

		return filmCopy;
	}

	public long saveFilm(String filmName, FilmType filmType) {
		return super.save(Film.create(filmName, filmType));
	}

	public Film findByName(String name) {
		EntityManager em = entityManagerThreadLocal.get();

		TypedQuery<Film> q = em.createNamedQuery("Film.findByName", Film.class);
		q.setParameter("_name", name);
		try {
			return q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public Set<FilmCopy> getFilmCopies(long filmId) {
		EntityManager em = entityManagerThreadLocal.get();
		Film film = em.find(Film.class, filmId);
		return film.getCopies();
	}

}
