package de.minee.util;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;

import de.minee.datamodel.Category;
import de.minee.datamodel.Category2;

public class ReflectionUtilTest {

	@Test
	public void testGetDeclaredField() throws NoSuchFieldException, SecurityException {
		final String fieldName = "parent";
		final Field actual = ReflectionUtil.getDeclaredField(Category.class, fieldName);

		final Field expected = Category.class.getDeclaredField(fieldName);

		// Expect that ReflectionUtil returns the same Field as from
		// Class.getDeclaredField
		// but without thrown Exception in case of a not found field
		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetDeclaredFieldNotFound() throws NoSuchFieldException, SecurityException {
		final String fieldName = "foobar";
		final Field actual = ReflectionUtil.getDeclaredField(Category.class, fieldName);

		// Ensure field is not there
		Throwable throwable = null;
		Field expected = null;
		try {
			expected = Category.class.getDeclaredField(fieldName);
		} catch (final NoSuchFieldException e) {
			throwable = e;
		}

		Assert.assertNull(actual);
		Assert.assertNull(expected);
		Assert.assertNotNull(throwable);
	}

	@Test
	public void testGetDeclaredFieldSuperClass() throws NoSuchFieldException, SecurityException {
		final String fieldName = "parent";
		final Field actual = ReflectionUtil.getDeclaredField(Category2.class, fieldName);

		// Check that field 'parent' is not declared for Category2
		Field nonDeclaredField = null;
		Throwable throwable = null;
		try {
			nonDeclaredField = Category2.class.getDeclaredField(fieldName);
		} catch (final NoSuchFieldException e) {
			throwable = e;
		}
		Assert.assertNull(nonDeclaredField);
		Assert.assertNotNull(throwable);

		final Field expected = Category.class.getDeclaredField(fieldName);

		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual);
	}

}
