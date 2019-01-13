package de.minee.util;

public final class Assertions {

	private Assertions() {
		// Static Class don't need an instance.
	}

	/**
	 * Ensures that a specific object o is not null. Throws an
	 * IllegalArgumentException if the object is null.
	 *
	 * @param object Object for check that is not null
	 */
	public static void assertNotNull(final Object object) {
		if (object == null) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Assert that a String is not null or empty.
	 *
	 * @param string
	 */
	public static void assertNotEmpty(final String string) {
		if (string == null || "".equals(string.trim())) {
			throw new IllegalArgumentException();
		}
	}
}
