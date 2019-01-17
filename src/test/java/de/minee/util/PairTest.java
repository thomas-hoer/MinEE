package de.minee.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

public class PairTest {

	private static final BigDecimal OBJECT_2 = BigDecimal.ONE;
	private static final String OBJECT_1 = "a";
	private static final Integer OBJECT_3 = Integer.valueOf(2);
	private static final KnownHashCode OBJECT_4 = new KnownHashCode(31);
	private static final KnownHashCode OBJECT_5 = new KnownHashCode(1);
	private static final KnownHashCode OBJECT_6 = new KnownHashCode(0);

	@Test
	public void testPair() {
		final Pair<String, BigDecimal> pair = new Pair<>(OBJECT_1, OBJECT_2);
		assertEquals(OBJECT_1, pair.first());
		assertEquals(OBJECT_2, pair.second());
	}

	@Test
	public void testPairEquals() {
		final Pair<String, BigDecimal> pair1 = new Pair<>(OBJECT_1, OBJECT_2);
		final Pair<String, BigDecimal> pair2 = new Pair<>(OBJECT_1, OBJECT_2);
		assertTrue(pair1.equals(pair2));
		assertEquals(pair1.hashCode(), pair2.hashCode());
		assertTrue(pair1.equals(pair1));
		assertFalse(pair1.equals(null));
		assertFalse(pair1.equals(1));
	}

	@Test
	public void testPairEqualsWithNull() {
		final Pair<String, Integer> pair1 = new Pair<>(null, null);
		final Pair<String, Boolean> pair2 = new Pair<>(null, null);
		assertTrue(pair1.equals(pair2));
		assertEquals(pair1.hashCode(), pair2.hashCode());
	}

	@Test
	public void testPairNotEquals1() {
		final Pair<String, BigDecimal> pair1 = new Pair<>(OBJECT_1, OBJECT_2);
		final Pair<Integer, BigDecimal> pair2 = new Pair<>(OBJECT_3, OBJECT_2);
		assertFalse(pair1.equals(pair2));
	}

	@Test
	public void testPairNotEquals2() {
		final Pair<String, BigDecimal> pair1 = new Pair<>(OBJECT_1, null);
		final Pair<String, BigDecimal> pair2 = new Pair<>(OBJECT_1, OBJECT_2);
		assertFalse(pair1.equals(pair2));
	}

	@Test
	public void testPairNotEquals3() {
		final Pair<String, BigDecimal> pair1 = new Pair<>(null, null);
		final Pair<String, BigDecimal> pair2 = new Pair<>(null, OBJECT_2);
		assertFalse(pair1.equals(pair2));
	}

	@Test
	public void testPairNotEquals4() {
		final Pair<String, KnownHashCode> pair1 = new Pair<>(OBJECT_1, OBJECT_4);
		final Pair<String, KnownHashCode> pair2 = new Pair<>(OBJECT_1, OBJECT_5);
		assertFalse(pair1.equals(pair2));
	}

	@Test
	public void testHashCode() {
		final Pair<KnownHashCode, KnownHashCode> pair1 = new Pair<>(OBJECT_6, OBJECT_4);
		final Pair<KnownHashCode, KnownHashCode> pair2 = new Pair<>(OBJECT_5, OBJECT_6);
		assertEquals(992, pair1.hashCode());
		assertEquals(992, pair2.hashCode());
		assertFalse(pair1.equals(pair2));
	}

	@Test
	public void testToString() {
		final Pair<String, BigDecimal> pair = new Pair<>(OBJECT_1, OBJECT_2);
		assertTrue(pair.toString().contains(OBJECT_1.toString()));
		assertTrue(pair.toString().contains(OBJECT_2.toString()));
	}

	private static class KnownHashCode {

		private final int hash;

		public KnownHashCode(final int hash) {
			this.hash = hash;
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(final Object other) {
			return other != null && other instanceof KnownHashCode && ((KnownHashCode) other).hash == this.hash;
		}
	}
}
