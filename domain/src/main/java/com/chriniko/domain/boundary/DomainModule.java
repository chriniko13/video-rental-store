package com.chriniko.domain.boundary;

import com.chriniko.domain.repository.CustomerRepository;
import com.chriniko.domain.repository.FilmCopyRepository;
import com.chriniko.domain.repository.FilmRepository;
import com.google.inject.AbstractModule;

public class DomainModule extends AbstractModule {

	@Override protected void configure() {
		bind(FilmRepository.class).asEagerSingleton();
		bind(CustomerRepository.class).asEagerSingleton();
		bind(FilmCopyRepository.class).asEagerSingleton();
	}
}

