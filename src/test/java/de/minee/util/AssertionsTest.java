package de.minee.util;

import org.junit.Assert;
import org.junit.Test;

public class AssertionsTest {

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
		Assertions.assertNotEmpty(test);
		Assert.assertNotNull(test);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertNotEmptyNull() {
		Assertions.assertNotEmpty(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertNotEmptyEmpty() {
		Assertions.assertNotEmpty("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertNotEmptySpaces() {
		Assertions.assertNotEmpty("   ");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertNotEmptyTab() {
		Assertions.assertNotEmpty("\t");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertNotEmptyNewLine() {
		Assertions.assertNotEmpty("\n\r");
	}
}
