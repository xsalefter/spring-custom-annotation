package com.baeldung;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleLoggingTest {

	private static final Logger logger = LoggerFactory.getLogger(SimpleLoggingTest.class);

	@Test
	public void test() {
		logger.info("This message should be appear although we don't configure anything.");
	}
}
