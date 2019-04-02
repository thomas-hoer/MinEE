package de.minee.util;

import org.junit.Assert;
import org.junit.Test;

public class AssertionsTest {

	private static final String MSG = "Example Message";

	@Test
	public void testAssertNotNullPositive() {
		final Object object = new Object();
		Assertions.assertNotNull(object);
		Assert.assertNotNull(object);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertNotNullWithNull() {
		Assertions.assertNotNull(null);

	}

	@Test
	public void testAssertNotEmptyPositive() {
		final String test = "Test";
		Assertions.assertNotEmpty(test, MSG);
		Assert.assertNotNull(test);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertNotEmptyNull() {
		Assertions.assertNotEmpty(null, MSG);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertNotEmptyEmpty() {
		Assertions.assertNotEmpty("", MSG);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertNotEmptySpaces() {
		Assertions.assertNotEmpty("   ", MSG);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertNotEmptyTab() {
		Assertions.assertNotEmpty("\t", MSG);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertNotEmptyNewLine() {
		Assertions.assertNotEmpty("\n\r", MSG);
	}

	public void testGetMessage() {
		try {
			Assertions.assertNotEmpty("", MSG);
		} catch (final IllegalArgumentException e) {
			Assert.assertEquals(MSG, e.getMessage());
		}
	}
}
