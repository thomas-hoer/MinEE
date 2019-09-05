package de.minee.util;

public final class Assertions {

	private Assertions() {
		// Static Class don't need an instance.
	}

	/**
	 * Ensures that a specific object o is not null. Throws an
	 * IllegalArgumentException if the object is null.
	 *
	 * @param object       Object for check that is not null
	 * @param errorMessage Message attached to the exception.
	 */
	public static void assertNotNull(final Object object, final String errorMessage) {
		if (object == null) {
			throw new IllegalArgumentException(errorMessage);
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
	 * @param string       String to be checked.
	 * @param errorMessage Message attached to the exception.
	 */
	public static void assertNotEmpty(final String string, final String errorMessage) {
		if (string == null || "".equals(string.trim())) {
			throw new IllegalArgumentException(errorMessage);
		}
	}

	public static void assertFalse(final boolean bool, final String errorMessage) {
		if (bool) {
			throw new IllegalStateException(errorMessage);
		}
	}

	public static void assertTrue(final boolean bool, final String errorMessage) {
		if (!bool) {
			throw new IllegalStateException(errorMessage);
		}
	}
}
