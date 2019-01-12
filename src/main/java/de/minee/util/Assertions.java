package de.minee.util;

public final class Assertions {

	private Assertions() {
	}

	/**
	 *
	 * @param o
	 */
	public static void assertNotNull(final Object o) {
		if (o == null) {
			throw new IllegalArgumentException();
		}
	}
}
