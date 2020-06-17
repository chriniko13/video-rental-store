package com.chriniko.e2e.infra;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileLoader {

	public static String load(String resourceName) {

		try {
			return String.join("", Files.readAllLines(Paths.get(FileLoader.class.getResource(resourceName).toURI())));
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
