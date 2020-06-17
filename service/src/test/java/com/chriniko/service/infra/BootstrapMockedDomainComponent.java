package com.chriniko.service.infra;

import com.chriniko.common.infra.BootstrapComponentModuleOnly;
import com.google.inject.AbstractModule;

import java.util.Optional;

public class BootstrapMockedDomainComponent extends BootstrapComponentModuleOnly {

	@Override public Optional<AbstractModule> getComponentModule() {
		return Optional.of(new MockedDomainModule());
	}
}
