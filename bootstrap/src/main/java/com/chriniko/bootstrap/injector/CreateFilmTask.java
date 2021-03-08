package com.chriniko.bootstrap.injector;

import java.util.List;

public final class CreateFilmTask {

	private final List<String> lines;

	CreateFilmTask(List<String> lines) {
		this.lines = lines;
	}

	public List<String> getLines() {
		return lines;
	}

}
