package de.minee.util;

import de.minee.datamodel.EnumObject;
import de.minee.datamodel.RecursiveObject;
import de.minee.datamodel.RecursiveObjectDerivation;
import de.minee.datamodel.ReferenceChain;
import de.minee.datamodel.SimpleReference;
import de.minee.jpa.Cascade;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class ReflectionUtilTest {

	private static final String NEW_NAME = "New Name";
	private static final UUID RANDOM_UUID = UUID.randomUUID();
	private static final UUID RANDOM_UUID_2 = UUID.randomUUID();

	@Test
	public void testGetDeclaredField() throws NoSuchFieldException, SecurityException {
		final String fieldName = "child";
		final Field actual = ReflectionUtil.getDeclaredField(RecursiveObject.class, fieldName);

		final Field expected = RecursiveObject.class.getDeclaredField(fieldName);

		// Expect that ReflectionUtil returns the same Field as from
		// Class.getDeclaredField
		// but without thrown Exception in case of a not found field
		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetDeclaredFieldNotFound() {
		final String fieldName = "foobar";
		final Field actual = ReflectionUtil.getDeclaredField(RecursiveObject.class, fieldName);

		// Ensure field is not there
		Throwable throwable = null;
		Field expected = null;
		try {
			expected = RecursiveObject.class.getDeclaredField(fieldName);
		} catch (final NoSuchFieldException e) {
			throwable = e;
		}

		Assert.assertNull(actual);
		Assert.assertNull(expected);
		Assert.assertNotNull(throwable);
	}

	@Test
	public void testGetDeclaredFieldSuperClass() throws NoSuchFieldException, SecurityException {
		final String fieldName = "child";
		final Field actual = ReflectionUtil.getDeclaredField(RecursiveObjectDerivation.class, fieldName);

		// Check that field 'parent' is not declared for Category2
		Field nonDeclaredField = null;
		Throwable throwable = null;
		try {
			nonDeclaredField = RecursiveObjectDerivation.class.getDeclaredField(fieldName);
		} catch (final NoSuchFieldException e) {
			throwable = e;
		}
		Assert.assertNull(nonDeclaredField);
		Assert.assertNotNull(throwable);

		final Field expected = RecursiveObject.class.getDeclaredField(fieldName);

		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testExecuteGet() throws NoSuchFieldException, SecurityException {
		final SimpleReference object = createObject();
		final Field idField = SimpleReference.class.getDeclaredField("id");
		final Field nameField = SimpleReference.class.getDeclaredField("name");

		final Object id = ReflectionUtil.executeGet(idField, object);
		final Object name = ReflectionUtil.executeGet(nameField, object);

		Assert.assertEquals(RANDOM_UUID, id);
		Assert.assertEquals("Name", name);
	}

	private static SimpleReference createObject() {
		final SimpleReference object = new SimpleReference();
		object.setId(RANDOM_UUID);
		object.setName("Name");
		object.setValue(null);
		object.setContent(new EnumObject());
		object.setReferenceChain(new ReferenceChain());
		return object;
	}

	@Test
	public void testExecuteGetNonMatchingField() throws NoSuchFieldException, SecurityException {
		final SimpleReference object = createObject();

		final Field idField = RecursiveObject.class.getDeclaredField("id");

		final Object id = ReflectionUtil.executeGet(idField, object);

		Assert.assertEquals(RANDOM_UUID, object.getId());
		Assert.assertNull(id);
	}

	@Test
	public void testExecuteSet() throws NoSuchFieldException, SecurityException {
		final SimpleReference object = createObject();

		final Field idField = SimpleReference.class.getDeclaredField("id");
		final Field nameField = SimpleReference.class.getDeclaredField("name");

		Assert.assertNotEquals(RANDOM_UUID_2, object.getId());
		Assert.assertNotEquals(NEW_NAME, object.getName());

		ReflectionUtil.executeSet(idField, object, RANDOM_UUID_2);
		ReflectionUtil.executeSet(nameField, object, NEW_NAME);

		Assert.assertEquals(RANDOM_UUID_2, object.getId());
		Assert.assertEquals(NEW_NAME, object.getName());
	}

	@Test
	public void testExecuteSetWrongType() throws NoSuchFieldException, SecurityException {
		final SimpleReference object = createObject();

		final Field idField = SimpleReference.class.getDeclaredField("id");

		Assert.assertEquals(RANDOM_UUID, object.getId());

		final boolean result = ReflectionUtil.executeSet(idField, object, NEW_NAME);

		Assert.assertFalse(result);
		Assert.assertEquals(RANDOM_UUID, object.getId());
	}

	@Test
	public void testExecuteSetNull() throws NoSuchFieldException, SecurityException {
		final SimpleReference object = createObject();

		final Field idField = SimpleReference.class.getDeclaredField("id");

		Assert.assertEquals(RANDOM_UUID, object.getId());

		final boolean result = ReflectionUtil.executeSet(idField, object, null);

		Assert.assertTrue(result);
		Assert.assertNull(object.getId());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testExecuteSetNullField() {
		final Object object = new Object();

		ReflectionUtil.executeSet(null, object, "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testExecuteSetNullObject() throws NoSuchFieldException, SecurityException {
		final Field idField = SimpleReference.class.getDeclaredField("id");

		ReflectionUtil.executeSet(null, idField, "");
	}

	@Test
	public void testGetAll() {
		final SimpleReference object = createObject();

		final Map<String, Object> values = ReflectionUtil.getAll(object);

		Assert.assertNotNull(values);
		Assert.assertEquals(5, values.size());
		Assert.assertNotNull(values.get("id"));
		Assert.assertNotNull(values.get("name"));
		Assert.assertNotNull(values.get("content"));
		Assert.assertNotNull(values.get("referenceChain"));
		Assert.assertEquals(object.getId(), values.get("id"));
		Assert.assertEquals(object.getName(), values.get("name"));
		Assert.assertEquals(object.getValue(), values.get("value"));
		Assert.assertEquals(object.getContent(), values.get("content"));
		Assert.assertEquals(object.getReferenceChain(), values.get("referenceChain"));
	}

	@Test
	public void testGetAllFieldsObject() {
		final List<Field> fields = ReflectionUtil.getAllFields(Object.class);
		Assert.assertTrue(fields.isEmpty());
	}

	@Test
	public void testGetAllFieldsDataObject() {
		final List<Field> fields = ReflectionUtil.getAllFields(SimpleReference.class);
		Assert.assertEquals(5, fields.size());
	}

	@Test
	public void testGetAllFieldsEnum() {
		final List<Field> fields = ReflectionUtil.getAllFields(Cascade.class);
		// Each enum value + array VALUES
		Assert.assertEquals(6, fields.size());
	}

	@Test
	public void testGetAllPlainObject() {
		final Object object = new Object();

		final Map<String, Object> values = ReflectionUtil.getAll(object);

		Assert.assertNotNull(values);
		Assert.assertEquals(0, values.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetAllNull() {
		ReflectionUtil.getAll(null);
	}
}
