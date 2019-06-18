package de.minee.jpa;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.minee.datamodel.ArrayTypes;
import de.minee.datamodel.EnumObject;
import de.minee.datamodel.PrimitiveList;
import de.minee.datamodel.RecursiveObject;
import de.minee.datamodel.ReferenceChain;
import de.minee.datamodel.ReferenceList;
import de.minee.datamodel.SimpleReference;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class PreparedDeleteTest extends AbstractTestDAO {

	public static final UUID ID_1 = UUID.randomUUID();

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
	public void testDelete() throws SQLException {
		final SimpleReference simpleReference = new SimpleReference();

		insert(simpleReference);

		final SimpleReference selectedElement = select(SimpleReference.class).byId(simpleReference.getId());

		delete(selectedElement);

		final SimpleReference deletedElement = select(SimpleReference.class).byId(simpleReference.getId());

		Assert.assertNotNull(selectedElement);
		Assert.assertNull(deletedElement);
	}

	@Test
	public void testDeleteShallow() throws SQLException {
		final SimpleReference simpleReference = new SimpleReference();
		final ReferenceChain referenceChain = new ReferenceChain();
		simpleReference.setReferenceChain(referenceChain);

		insert(simpleReference);

		final SimpleReference selectedElement = select(SimpleReference.class).byId(simpleReference.getId());

		deleteShallow(selectedElement);

		final SimpleReference deletedElement = select(SimpleReference.class).byId(simpleReference.getId());
		final ReferenceChain selectedReference = select(ReferenceChain.class).byId(referenceChain.getId());
		Assert.assertNotNull(selectedElement);
		Assert.assertNull(deletedElement);
		Assert.assertNotNull(selectedReference);
	}

	@Test
	public void testDeleteCascade() throws SQLException {
		final SimpleReference simpleReference = new SimpleReference();
		final ReferenceChain referenceChain = new ReferenceChain();
		simpleReference.setReferenceChain(referenceChain);

		insert(simpleReference);

		final SimpleReference selectedElement = select(SimpleReference.class).byId(simpleReference.getId());

		delete(selectedElement);

		final SimpleReference deletedElement = select(SimpleReference.class).byId(simpleReference.getId());
		final ReferenceChain selectedReference = select(ReferenceChain.class).byId(referenceChain.getId());
		Assert.assertNotNull(selectedElement);
		Assert.assertNull(deletedElement);
		Assert.assertNull(selectedReference);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNotAllowedCascadeInsert() throws SQLException {
		new PreparedDelete<>(SimpleReference.class, getConnection(), Cascade.INSERT);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNotAllowedCascadeMerge() throws SQLException {
		new PreparedDelete<>(SimpleReference.class, getConnection(), Cascade.MERGE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNotAllowedCascadeUpdate() throws SQLException {
		new PreparedDelete<>(SimpleReference.class, getConnection(), Cascade.UPDATE);
	}

	@Test
	public void testDeletePrimitiveList() throws SQLException {
		final PrimitiveList primitiveList = new PrimitiveList();
		primitiveList.setIntList(Arrays.asList(1, 2, 3));
		primitiveList.setId(ID_1);

		insert(primitiveList);

		primitiveList.setIntList(Arrays.asList(1, 3));

		delete(primitiveList);

		final List<PrimitiveList> selectedElements = select(PrimitiveList.class).execute();

		assertTrue(selectedElements.isEmpty());

		final PrimitiveList newPrimitiveList = new PrimitiveList();
		newPrimitiveList.setIntList(Arrays.asList(4, 5));
		newPrimitiveList.setId(ID_1);

		insert(newPrimitiveList);

		final PrimitiveList selectedElement = select(PrimitiveList.class).byId(ID_1);
		assertNotNull(selectedElement);
		assertArrayEquals(new Integer[] { 4, 5 }, selectedElement.getIntList().toArray(new Integer[] {}));
	}

	@Test
	public void testDeleteList() throws SQLException {
		final ReferenceList referenceList = new ReferenceList();
		final RecursiveObject recursiveObject1 = new RecursiveObject();
		final RecursiveObject recursiveObject2 = new RecursiveObject();
		referenceList.setRecursiveObjects(Arrays.asList(recursiveObject1, recursiveObject2));

		insert(referenceList);

		delete(referenceList);

		final List<RecursiveObject> selectedRecursiveObjects = select(RecursiveObject.class).execute();
		final List<ReferenceList> selectedReferenceLists = select(ReferenceList.class).execute();

		assertTrue(selectedRecursiveObjects.isEmpty());
		assertTrue(selectedReferenceLists.isEmpty());
	}

	@Test
	public void testDeleteListReferences() throws SQLException {
		final ReferenceList referenceList = new ReferenceList();
		final RecursiveObject recursiveObject1 = new RecursiveObject();
		final RecursiveObject recursiveObject2 = new RecursiveObject();
		referenceList.setId(ID_1);
		referenceList.setRecursiveObjects(Arrays.asList(recursiveObject1, recursiveObject2));

		insert(referenceList);

		referenceList.setRecursiveObjects(Arrays.asList(recursiveObject1));

		delete(referenceList);

		final List<RecursiveObject> selectedRecursiveObjects = select(RecursiveObject.class).execute();
		final List<ReferenceList> selectedReferenceLists = select(ReferenceList.class).execute();

		assertTrue(selectedReferenceLists.isEmpty());
		assertEquals(1, selectedRecursiveObjects.size());

		final ReferenceList referenceList2 = new ReferenceList();
		final RecursiveObject recursiveObject3 = new RecursiveObject();
		referenceList2.setId(ID_1);
		referenceList2.setRecursiveObjects(Arrays.asList(recursiveObject3));
		insert(referenceList2);

		assertEquals(2, select(RecursiveObject.class).execute().size());
		final ReferenceList selectedReferenceList = select(ReferenceList.class).byId(ID_1);
		assertEquals(1, selectedReferenceList.getRecursiveObjects().size());
		assertEquals(recursiveObject3.getId(), selectedReferenceList.getRecursiveObjects().get(0).getId());
	}
}
