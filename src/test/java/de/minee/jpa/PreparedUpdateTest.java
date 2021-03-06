package de.minee.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import de.minee.datamodel.ArrayTypes;
import de.minee.datamodel.EnumObject;
import de.minee.datamodel.PrimitiveList;
import de.minee.datamodel.RecursiveObject;
import de.minee.datamodel.ReferenceChain;
import de.minee.datamodel.ReferenceList;
import de.minee.datamodel.SimpleReference;

import java.util.Arrays;

import org.junit.Test;

public class PreparedUpdateTest extends AbstractTestDAO {

	private static final String NAME_1 = "Name1";
	private static final String NAME_2 = "Name2";

	@Override
	protected int updateDatabaseSchema(final int oldDbSchemaVersion) {
		createTable(RecursiveObject.class);
		createTable(ReferenceList.class);
		createTable(SimpleReference.class);
		createTable(ReferenceChain.class);
		createTable(EnumObject.class);
		createTable(PrimitiveList.class);
		createTable(ArrayTypes.class);
		return 1;
	}

	@Test
	public void testSimpleUpdate() {
		final RecursiveObject recursiveObject = new RecursiveObject();
		recursiveObject.setName(NAME_1);
		insert(recursiveObject);
		final RecursiveObject selectedElement1 = select(RecursiveObject.class).byId(recursiveObject.getId());
		recursiveObject.setName(NAME_2);

		update(recursiveObject);

		final RecursiveObject selectedElement2 = select(RecursiveObject.class).byId(recursiveObject.getId());
		assertEquals(NAME_1, selectedElement1.getName());
		assertEquals(NAME_2, selectedElement2.getName());
	}

	@Test
	public void testUpdateReference() {
		final SimpleReference simpleReference = new SimpleReference();
		final ReferenceChain referenceChain = new ReferenceChain();
		referenceChain.setName(NAME_1);
		simpleReference.setReferenceChain(referenceChain);

		insert(simpleReference);

		final ReferenceChain selectedReferenceChain = select(ReferenceChain.class).byId(referenceChain.getId());
		assertNotNull(selectedReferenceChain);
		assertEquals(NAME_1, selectedReferenceChain.getName());

		referenceChain.setName(NAME_2);

		update(simpleReference);

		final SimpleReference selectedElement = select(SimpleReference.class).byId(simpleReference.getId());
		assertNotNull(selectedElement);
		assertNotNull(selectedElement.getReferenceChain());
		assertEquals(NAME_2, selectedElement.getReferenceChain().getName());
	}

	@Test
	public void testUpdateShallow() {
		final SimpleReference simpleReference = new SimpleReference();
		simpleReference.setName(NAME_1);

		insert(simpleReference);

		final ReferenceChain referenceChain = new ReferenceChain();
		referenceChain.setName(NAME_1);
		simpleReference.setReferenceChain(referenceChain);
		simpleReference.setName(NAME_2);

		updateShallow(simpleReference);

		final SimpleReference selectedElement = select(SimpleReference.class).byId(simpleReference.getId());
		assertNotNull(selectedElement);
		assertNull(selectedElement.getReferenceChain());
		assertEquals(NAME_2, selectedElement.getName());
	}

	@Test
	public void testUpdateList() {
		final ReferenceList referenceList = new ReferenceList();
		referenceList.setDescription(NAME_1);
		final RecursiveObject recursiveObject = new RecursiveObject();
		referenceList.setRecursiveObjects(Arrays.asList(recursiveObject));

		insert(referenceList);

		referenceList.setName(NAME_1);
		referenceList.setDescription(null);
		recursiveObject.setName(NAME_2);

		update(referenceList);

		final ReferenceList selectedElement = select(ReferenceList.class).byId(referenceList.getId());

		assertNotNull(selectedElement);
		assertEquals(NAME_1, selectedElement.getName());
		assertNull(NAME_1, selectedElement.getDescription());
		assertEquals(1, selectedElement.getRecursiveObjects().size());
		assertEquals(NAME_2, selectedElement.getRecursiveObjects().get(0).getName());
	}

	@Test(expected = DatabaseException.class)
	public void testUpdateListFailureInsert() {
		final ReferenceList referenceList = new ReferenceList();
		final RecursiveObject recursiveObject = new RecursiveObject();
		referenceList.setRecursiveObjects(Arrays.asList(recursiveObject));

		insert(referenceList);

		referenceList.setRecursiveObjects(Arrays.asList(recursiveObject, new RecursiveObject()));

		update(referenceList);
	}

}
