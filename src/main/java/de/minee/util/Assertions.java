package de.minee.util;

public final class Assertions {

	private Assertions() {
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
}
