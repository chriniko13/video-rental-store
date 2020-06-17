package com.chriniko.common.infra;

import com.google.inject.AbstractModule;

import java.util.ArrayList;

public class ModuleList extends ArrayList<AbstractModule> {

	public ModuleList(Iterable<? extends BootstrapComponent> components) {
		super();
		components.forEach(component -> component.getComponentModule().ifPresent(this::add));
	}
}
