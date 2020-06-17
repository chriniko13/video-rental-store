package com.chriniko.common.infra;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Clock;
import java.time.ZoneId;

public class ClockProvider {

	private static Clock clock;

	static {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				ClockProvider.class.getResourceAsStream("/clock.properties")))) {
			String firstLine = br.readLine();
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
