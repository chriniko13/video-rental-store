package com.chriniko.bootstrap;

import org.apache.log4j.Logger;

public class Main {

	private static final Logger logger = Logger.getLogger(Main.class);

	// Note: application entry point
	public static void main(String[] args) {
		try {
			Bootstrap.run(true);
		} catch (Exception e) {
			logger.error("unknown exception occurred, msg: " + e.getMessage(), e);
			System.exit(-1);
		}
	}

}
