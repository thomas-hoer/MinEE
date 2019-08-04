package de.minee.rest.renderer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.minee.datamodel.ArrayTypes;
import de.minee.datamodel.EnumObject;
import de.minee.datamodel.RecursiveObject;
import de.minee.datamodel.ReferenceChain;
import de.minee.datamodel.ReferenceList;
import de.minee.datamodel.SimpleReference;
import de.minee.datamodel.enumeration.Enumeration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class JsonRendererTest {

	private static final UUID ID_1 = UUID.fromString("7bfccade-7b6f-4466-9c8a-b43ddef2588b");
	private static final UUID ID_2 = UUID.fromString("cd8ea07c-2274-485f-882d-f5093071f764");
	private static final UUID ID_3 = UUID.fromString("1464badf-299c-37da-a847-263fb70fa216");

	private final JsonRenderer renderer = new JsonRenderer();

	@Test
	public void testRenderNull() {
		final String result = renderer.render(null);

		Assert.assertNotNull(result);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForCreateNull() {
		renderer.forCreate(null);
	}

	@Test
	public void testForEditNull() {
		final String result = renderer.forEdit(null);
		Assert.assertNotNull(result);
	}

	@Test
	public void testForCreateEnum() {
		final String result = renderer.forCreate(EnumObject.class);
		Assert.assertTrue(result.contains("\"type\":\"Enum\""));
		Assert.assertTrue(result.contains("[\"ENUM_VALUE_1\",\"ENUM_VALUE_2\",\"ENUM_VALUE_3\",\"ENUM_VALUE_4\",\"ENUM_VALUE_5\",\"ENUM_VALUE_6\"]"));
	}

	@Test
	public void testRender() {
		final Object o = createExampleObject();
		final String output = renderer.render(o);
		assertNotNull(output);
		assertTrue(output.contains(ID_1.toString()));
		assertTrue(output.contains(ID_2.toString()));
	}

	@Test
	public void testRenderArray() {
		final Object o = createExampleObject();
		final String output = renderer.render(new Object[] { o });
		assertNotNull(output);
		assertTrue(output.contains(ID_1.toString()));
		assertTrue(output.contains(ID_2.toString()));
	}

	@Test
	public void testRenderArrayNull() {
		final ArrayTypes arrayTypes = new ArrayTypes();
		arrayTypes.setIntArray(new Integer[] { 7, null, 41 });
		final String output = renderer.render(arrayTypes);
		assertTrue(output.contains("7"));
		assertTrue(output.contains("null"));
		assertTrue(output.contains("41"));
	}

	/**
	 * A reference cycle shall not cause a StackOverflowException.
	 */
	@Test
	public void testRenderCyclic() {
		final Object o = createCyclicObject();
		final String output = renderer.render(o);
		final String expected = "{\"id\":\""+ID_3.toString()+"\",\"child\":\""+ID_3.toString()+"\",\"child2\":\""+ID_3.toString()+"\"}";
		assertEquals(expected , output);
	}

	/**
	 * This test is needed because the cycle avoidance skip to render objects if
	 * they are already rendered. In the beginning it skipped to much.
	 */
	@Test
	public void testRenderListWithIdenticalObjects() {
		final ReferenceList object = new ReferenceList();
		object.setId(ID_1);
		final List<RecursiveObject> list = new ArrayList<>();
		final RecursiveObject recursiveObject = new RecursiveObject();
		recursiveObject.setId(ID_2);
		list.add(recursiveObject);
		list.add(recursiveObject);
		object.setRecursiveObjects(list);

		final String output = renderer.render(object);
		// Assert 2 occurrences of ID_2
		assertEquals(3, output.split(ID_2.toString()).length);
	}

	@Test
	public void testForCreateRecursive() {
		final String output = renderer.forCreate(RecursiveObject.class);
		assertNotNull(output);
		assertTrue(output.contains("child"));
		assertTrue(output.contains("\"type\":\"RecursiveObject\""));
		assertTrue(output.contains("\"child\":\"RecursiveObject\""));
		assertTrue(output.contains("id"));
		assertTrue(output.contains("{\"type\":\"UUID\"}"));
		assertTrue(output.contains("\"name\":{\"type\":\"String\"}"));
	}

	@Test
	public void testForCreateHierarchy() {
		final String output = renderer.forCreate(ReferenceChain.class);
		assertTrue(output.contains("simpleReference"));
		assertTrue(output.contains("SimpleReference"));
		assertTrue(output.contains("id"));
		assertTrue(output.contains("UUID"));
		assertTrue(output.contains("name"));
		assertTrue(output.contains("String"));
	}

	@Test
	public void testForEdit() {
		final Object o = createExampleObject();
		final String output = renderer.forEdit(o);
		assertNotNull(output);
		assertTrue(output.contains(ID_1.toString()));
		assertTrue(output.contains(ID_2.toString()));
	}

	@Test
	public void testEnum() {
		final EnumObject enumObject = new EnumObject();
		enumObject.setEnumeration(Enumeration.ENUM_VALUE_1);
		final String output1 = renderer.render(enumObject);
		assertTrue(output1.contains(Enumeration.ENUM_VALUE_1.name()));
		enumObject.setEnumeration(Enumeration.ENUM_VALUE_2);
		final String output2 = renderer.render(enumObject);
		assertTrue(output2.contains(Enumeration.ENUM_VALUE_2.name()));
	}

	@Test
	public void testBoolean1() {
		final TestBoolean testBoolean = new TestBoolean();
		testBoolean.bool1 = true;
		testBoolean.bool2 = false;
		final String actual = renderer.render(testBoolean);
		assertEquals("{\"bool1\":true,\"bool2\":false}", actual);
	}

	@Test
	public void testBoolean2() {
		final TestBoolean testBoolean = new TestBoolean();
		testBoolean.bool1 = false;
		testBoolean.bool2 = true;
		final String actual = renderer.render(testBoolean);
		assertEquals("{\"bool1\":false,\"bool2\":true}", actual);
	}

	@Test
	public void testBoolean3() {
		final TestBoolean testBoolean = new TestBoolean();
		testBoolean.bool1 = true;
		final String actual = renderer.render(testBoolean);
		assertEquals("{\"bool1\":true}", actual);
	}

	@Test
	public void testInteger1() {
		final TestInteger testInt = new TestInteger();
		final String actual = renderer.render(testInt);
		assertEquals("{\"int1\":0}", actual);
	}

	@Test
	public void testInteger2() {
		final TestInteger testInt = new TestInteger();
		testInt.int1 = 1;
		testInt.int2 = -1;
		final String actual = renderer.render(testInt);
		assertEquals("{\"int1\":1,\"int2\":-1}", actual);
	}

	@Test
	public void testInteger3() {
		final TestInteger testInt = new TestInteger();
		testInt.int1 = 42;
		testInt.int2 = Integer.MAX_VALUE;
		final String actual = renderer.render(testInt);
		assertEquals("{\"int1\":42,\"int2\":2147483647}", actual);
	}
	@Test
	public void testEscapeString() {
		final SimpleReference sr = new SimpleReference();
		final String escapeCharacters = "\"\\\t";
		sr.setName(escapeCharacters);
		final String output = renderer.render(sr);
		assertFalse(output.contains(escapeCharacters));
		assertTrue(output.contains("\\\"\\\\\\t"));
	}

	@Test
	public void testGetContentType() {
		final String contentType = renderer.getContentType();
		assertEquals("application/json", contentType);
	}

	static class TestBoolean {
		boolean bool1;
		Boolean bool2;
	}

	static class TestInteger {
		int int1;
		Integer int2;
	}

	private static Object createExampleObject() {
		final SimpleReference simpleReference = new SimpleReference();
		simpleReference.setId(ID_1);
		simpleReference.setValue("SR-Value");
		final ReferenceChain object = new ReferenceChain();
		object.setSimpleReference(simpleReference);
		object.setId(ID_2);
		object.setName("RC-Name");

		return object;
	}

	private static Object createCyclicObject() {
		final RecursiveObject object = new RecursiveObject();
		object.setId(ID_3);
		object.setChild(object);
		object.setChild2(object);

		return object;
	}

}
