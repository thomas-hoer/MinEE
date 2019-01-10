package de.minee.hateoes.renderer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.minee.datamodel.RecursiveObject;
import de.minee.datamodel.ReferenceChain;
import de.minee.datamodel.SimpleReference;

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
	public void testRenderCyclic() {
		final Object o = createCyclicObject();
		final String output = renderer.render(o);
		assertNotNull(output);
		assertTrue(output.contains(ID_3.toString()));
	}

	@Test
	public void testForCreateRecursive() {
		final String output = renderer.forCreate(RecursiveObject.class);
		assertNotNull(output);
		assertTrue(output.contains("child"));
		assertTrue(output.contains("RecursiveObject"));
		assertTrue(output.contains("id"));
		assertTrue(output.contains("UUID"));
		assertTrue(output.contains("name"));
		assertTrue(output.contains("String"));
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

		return object;
	}
}
