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
	 * <pre>
	 * Examples:
	 * assertNotEmpty("foo")-> no Exception
	 * assertNotEmpty("")   -> Exception
	 * assertNotEmpty("\n") -> Exception
	 * assertNotEmpty("\t") -> Exception
	 * assertNotEmpty("  ") -> Exception
	 * assertNotEmpty(null) -> Exception
	 * </pre>
	 *
	 * @param string String to be checked.
	 */
	public static void assertNotEmpty(final String string) {
		if (string == null || "".equals(string.trim())) {
			throw new IllegalArgumentException();
		}
	}
}
