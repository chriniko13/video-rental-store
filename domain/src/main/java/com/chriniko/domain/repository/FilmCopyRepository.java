package com.chriniko.domain.repository;

import com.chriniko.domain.FilmCopy;
import com.chriniko.domain.base.BaseRepository;

public class FilmCopyRepository extends BaseRepository<FilmCopy, Long> {

	@Override protected Class<FilmCopy> getEntityClass() {
		return FilmCopy.class;
	}
}
