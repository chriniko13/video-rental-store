package com.chriniko.service.infra;

import com.chriniko.domain.repository.FilmRepository;
import com.google.inject.AbstractModule;

public class MockedDomainModule extends AbstractModule {

	@Override protected void configure() {

		MockedFilmRepository mockedFilmRepository = new MockedFilmRepository();

		bind(FilmRepository.class).toInstance(mockedFilmRepository);
		bind(MockedFilmRepository.class).toInstance(mockedFilmRepository);
	}
}
