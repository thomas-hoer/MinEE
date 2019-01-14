package de.minee.env;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EnvironmentTest {

	@Test
	public void testGetEnvironmentVariable() {
		final String value = Environment.getEnvironmentVariable("key");
		assertEquals("", value);
	}

	@Test
	public void testGet() {
		final Environment environment = new Environment();
		final String value = environment.get("key");
		assertEquals("", value);
	}
}
