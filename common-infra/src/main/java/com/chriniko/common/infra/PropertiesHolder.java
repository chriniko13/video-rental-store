package com.chriniko.common.infra;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

public class PropertiesHolder {

	private static final Properties PROPERTIES = new Properties();

	static {

		String[] resources = { "domain.properties" };
		for (String resource : resources) {
			try (InputStream inputStream = PropertiesHolder.class.getClassLoader().getResourceAsStream(resource)) {
				PROPERTIES.load(inputStream);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	private PropertiesHolder() {
	}

	public static int getAsInt(String propName) {
		return Integer.parseInt(PROPERTIES.getProperty(propName));
	}

	public static double getAsDouble(String propName) {
		return Double.parseDouble(PROPERTIES.getProperty(propName));
	}

}
