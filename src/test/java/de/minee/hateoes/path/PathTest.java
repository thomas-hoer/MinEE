package de.minee.hateoes.path;

import de.minee.datamodel.SimpleReference;

import org.junit.Assert;
import org.junit.Test;

public class PathTest {

	private static final String PATH_ROOT = "/";
	private static final String PATH_EMPTY = "";
	private static final String PATH_PARAMETER = "/a/b/{id}/";
	private static final String PATH_FOO_BAR = "foo/bar";

	@Test
	public void testToStringRoot() {
		final Path<SimpleReference> path = new Path<>(null, PATH_ROOT, SimpleReference.class);
		final String toString = path.toString();
		Assert.assertEquals(PATH_EMPTY, toString);
	}

	@Test
	public void testToStringEmpty() {
		final Path<SimpleReference> path = new Path<>(null, PATH_EMPTY, SimpleReference.class);
		final String toString = path.toString();
		Assert.assertEquals(PATH_EMPTY, toString);
	}

	@Test
	public void testToStringParameter() {
		final Path<SimpleReference> path = new Path<>(null, PATH_PARAMETER, SimpleReference.class);
		final String toString = path.toString();
		Assert.assertEquals("/a/b/{id}", toString);
	}

	@Test
	public void testToStringConstant() {
		final Path<SimpleReference> path = new Path<>(null, PATH_FOO_BAR, SimpleReference.class);
		final String toString = path.toString();
		Assert.assertEquals(PATH_FOO_BAR, toString);
	}

	@Test
	public void testSizeRoot() {
		final Path<SimpleReference> path = new Path<>(null, PATH_ROOT, SimpleReference.class);
		Assert.assertEquals(0, path.size());
	}

	@Test
	public void testSizeEmpty() {
		final Path<SimpleReference> path = new Path<>(null, PATH_EMPTY, SimpleReference.class);
		Assert.assertEquals(1, path.size());
	}

	@Test
	public void testSizeParameter() {
		final Path<SimpleReference> path = new Path<>(null, PATH_PARAMETER, SimpleReference.class);
		Assert.assertEquals(4, path.size());
	}

	@Test
	public void testSizeConstant() {
		final Path<SimpleReference> path = new Path<>(null, PATH_FOO_BAR, SimpleReference.class);
		Assert.assertEquals(2, path.size());
	}

	@Test
	public void testIsMatchConstantPositive() {
		final Path<SimpleReference> path = new Path<>(null, PATH_FOO_BAR, SimpleReference.class);
		Assert.assertTrue(path.isMatch(PATH_FOO_BAR));
	}

	@Test
	public void testIsMatchConstantNegative() {
		final Path<SimpleReference> path = new Path<>(null, PATH_FOO_BAR, SimpleReference.class);
		Assert.assertFalse(path.isMatch("/foo/foo"));
	}

	@Test
	public void testIsMatchParameterPositive1() {
		final Path<SimpleReference> path = new Path<>(null, PATH_PARAMETER, SimpleReference.class);
		Assert.assertTrue(path.isMatch("/a/b/1"));
	}

	@Test
	public void testIsMatchParameterPositive2() {
		final Path<SimpleReference> path = new Path<>(null, PATH_PARAMETER, SimpleReference.class);
		Assert.assertTrue(path.isMatch("/a/b/2"));
	}

	@Test
	public void testIsMatchParameterNegative() {
		final Path<SimpleReference> path = new Path<>(null, PATH_PARAMETER, SimpleReference.class);
		Assert.assertFalse(path.isMatch("/foo/foo/foo"));
	}
}
