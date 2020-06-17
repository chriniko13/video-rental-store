package com.chriniko.e2e.infra;

import com.chriniko.bootstrap.Bootstrap;
import io.cucumber.java.Before;

public class GlobalHooks {

	private static final Object mutex = new Object();
	private static boolean dunit = false;

	@Before
	public void beforeAll() {

		synchronized (mutex) {
			if (!dunit) {
				// do the beforeAll stuff...
				Bootstrap.runTest();
				dunit = true;
			}
		}

	}
}
