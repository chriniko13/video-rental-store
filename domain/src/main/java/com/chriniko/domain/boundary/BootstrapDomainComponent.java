package com.chriniko.domain.boundary;

import com.chriniko.common.infra.BootstrapComponentModuleOnly;
import com.google.inject.AbstractModule;

import java.util.Optional;

public class BootstrapDomainComponent extends BootstrapComponentModuleOnly {

	@Override public Optional<AbstractModule> getComponentModule() {
		return Optional.of(new DomainModule());
	}
}
