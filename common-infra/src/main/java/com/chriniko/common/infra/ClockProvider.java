package com.chriniko.common.infra;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.ZoneId;

public class ClockProvider {

	private static Clock clock;

	static {
		try {
			URI uri = ClockProvider.class.getResource("/clock.properties").toURI();
			Path path = Paths.get(uri);
			String firstLine = Files.lines(path).iterator().next();
			String timezone = firstLine.split("=")[1];
			clock = Clock.system(ZoneId.of(timezone));
		} catch (Exception e) {
			throw new InfrastructureException(e);
		}
	}

	private ClockProvider() {
	}

	public static synchronized Clock getClock() {
		return clock;
	}

	public static synchronized void setClock(Clock clock) {
		ClockProvider.clock = clock;
	}
}
