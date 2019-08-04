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

public class HtmlRendererTest {

	private static final UUID ID_1 = UUID.fromString("7bfccade-7b6f-4466-9c8a-b43ddef2588b");
	private static final UUID ID_2 = UUID.fromString("cd8ea07c-2274-485f-882d-f5093071f764");
	private static final UUID ID_3 = UUID.fromString("1464badf-299c-37da-a847-263fb70fa216");

	private final HtmlRenderer renderer = new HtmlRenderer();

	@Test
	public void testRenderNull() {
		final String result = renderer.render(null);
		Assert.assertEquals("<!DOCTYPE html><html><body></body></html>", result);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForCreateNull() {
		renderer.forCreate(null);
	}

	@Test(expected = NullPointerException.class)
	public void testForEditNull() {
		renderer.forEdit(null);
	}

	@Test
	public void testRender() {
		final ReferenceChain o = createExampleObject();
		final String output = renderer.render(o);
		assertNotNull(output);
		assertTrue(output.contains("RC-Name"));
		assertTrue(output.contains("SR-Value"));
		assertFalse(output.contains(o.getId().toString()));
		assertFalse(output.contains(o.getSimpleReference().getId().toString()));
	}

	@Test
	public void testRenderArray() {
		final Object o = createExampleObject();
		final String output = renderer.render(new Object[] { o });
		assertNotNull(output);
		assertTrue(output.contains("RC-Name"));
		assertTrue(output.contains("SR-Value"));
	}

	@Test
	public void testRenderArrayNull() {
		final ArrayTypes arrayTypes = new ArrayTypes();
		arrayTypes.setIntArray(new Integer[] { 7, null, 41 });
		final String output = renderer.render(arrayTypes);
		assertTrue(output.contains("7"));
		assertTrue(output.contains("<div></div>"));
		assertTrue(output.contains("41"));
	}

	/**
	 * A reference cycle shall not cause a StackOverflowException.
	 */
	@Test
	public void testRenderCyclic() {
		final Object object = createCyclicObject();
		final String output = renderer.render(object);
		assertNotNull(output);
		assertTrue(output.contains(ID_3.toString()));
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
		assertEquals(3, output.split(RecursiveObject.class.getSimpleName()).length);
	}

	@Test
	public void testForCreateRecursive() {
		final String output = renderer.forCreate(RecursiveObject.class);
		assertNotNull(output);
		assertTrue(output.contains("child"));
		assertTrue(output.contains("id"));
		assertTrue(output.contains("name"));
	}

	@Test
	public void testForCreateHierarchy() {
		final String output = renderer.forCreate(ReferenceChain.class);
		assertTrue(output.contains("simpleReference"));
		assertTrue(output.contains("id"));
		assertTrue(output.contains("name"));
	}

	@Test
	public void testForEdit() {
		final ReferenceChain o = createExampleObject();
		final String output = renderer.forEdit(o);
		assertNotNull(output);
		assertTrue(output.contains("method=\"POST\""));
		assertTrue(output.contains(String.format("value=\"%s\"",ID_1)));
		assertTrue(output.contains(String.format("value=\"%s\"",ID_2)));
		assertTrue(output.contains(String.format("value=\"%s\"","RC-Name")));
		assertTrue(output.contains("name=\"simpleReference\""));
		assertTrue(output.contains("name=\"id"));
		assertTrue(output.contains("name=\"name"));
		assertTrue(output.contains("placeholder=\"simpleReference\""));
		assertTrue(output.contains("placeholder=\"id\""));
		assertTrue(output.contains("placeholder=\"name\""));

		// Only show the root object, referenced objects can not be modified directly
		assertFalse(output.contains(o.getSimpleReference().getValue()));
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
	public void testValidHtml() {
		final String output = renderer.render(new Object());
		assertEquals("<!DOCTYPE html><html><body><div class=\"Object\"></div></body></html>", output);
	}

	@Test
	public void testGetContentType() {
		final String contentType = renderer.getContentType();
		assertEquals("text/html", contentType);
	}

	private static ReferenceChain createExampleObject() {
		final SimpleReference simpleReference = new SimpleReference();
		simpleReference.setId(ID_1);
		simpleReference.setValue("SR-Value");
		final ReferenceChain object = new ReferenceChain();
		object.setSimpleReference(simpleReference);
		object.setId(ID_2);
		object.setName("RC-Name");

		return object;
	}

	private static RecursiveObject createCyclicObject() {
		final RecursiveObject object = new RecursiveObject();
		object.setId(ID_3);
		object.setChild(object);

		return object;
	}

}
