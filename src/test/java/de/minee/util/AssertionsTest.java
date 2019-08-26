package de.minee.util;

import org.junit.Assert;
import org.junit.Test;

public class AssertionsTest {

	private static final String MSG = "Example Message";

	@Test
	public void testAssertNotNullPositive() {
		final Object object = new Object();
		Assertions.assertNotNull(object, MSG);
		Assert.assertNotNull(object);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertNotNullWithNull() {
		Assertions.assertNotNull(null, MSG);

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

	public void testAssertFalseWithFalse() {
		final boolean input = false;
		Assertions.assertFalse(input, MSG);
		Assert.assertFalse(input);
	}

	@Test(expected = IllegalStateException.class)
	public void testAssertFalseWithTrue() {
		final boolean input = true;
		Assertions.assertFalse(input, MSG);
	}
}
