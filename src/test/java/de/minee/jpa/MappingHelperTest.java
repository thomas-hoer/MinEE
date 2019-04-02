package de.minee.jpa;

import de.minee.datamodel.ArrayTypes;
import de.minee.datamodel.PrimitiveList;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class MappingHelperTest {

	private int[] intArray;

	@Test
	public void testMapDatabaseTypeUUID() {
		final Field field = ReflectionUtil.getDeclaredField(PrimitiveList.class, "id");
		final String databaseType = MappingHelper.mapDatabaseType(field);
		Assert.assertEquals("UUID", databaseType);
	}

	@Test
	public void testMapDatabaseTypeIntList() {
		final Field field = ReflectionUtil.getDeclaredField(PrimitiveList.class, "intList");
		final String databaseType = MappingHelper.mapDatabaseType(field);
		Assert.assertNull(databaseType);
	}

	@Test
	public void testMapDatabaseTypeIntegerArray() {
		final Field field = ReflectionUtil.getDeclaredField(ArrayTypes.class, "intArray");
		final String databaseType = MappingHelper.mapDatabaseType(field);
		Assert.assertEquals("ARRAY", databaseType);
	}

	@Test(expected = MappingException.class)
	public void testMapDatabaseTypeIntArray() {
		final Field field = ReflectionUtil.getDeclaredField(MappingHelperTest.class, "intArray");
		MappingHelper.mapDatabaseType(field);
	}

	@Test
	public void testGetDbObjectObject() {
		final Object object = new Object();
		final Object dbObject = MappingHelper.getDbObject(object);
		final UUID expectedDbObject = UUID.fromString("00000000-0000-0000-0000-000000000000");
		Assert.assertEquals(expectedDbObject, dbObject);
	}

	@Test
	public void testGetDbObjectInteger() {
		final Integer object = Integer.valueOf(1);
		final Object dbObject = MappingHelper.getDbObject(object);
		Assert.assertEquals(object, dbObject);
	}

	@Test
	public void testGetDbObjectEnum() {
		final Cascade object = Cascade.NONE;
		final Object dbObject = MappingHelper.getDbObject(object);
		Assert.assertEquals("NONE", dbObject);
	}

	@Test
	public void testGetDbObjectList() {
		final List<?> object = new ArrayList<>();
		final Object dbObject = MappingHelper.getDbObject(object);
		Assert.assertNull(dbObject);
	}
}
