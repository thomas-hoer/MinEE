package de.minee.jpa;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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

import org.junit.Test;

public class PreparedMergeTest extends AbstractTestDAO {

	@Override
	protected int updateDatabaseSchema(final int oldDbSchemaVersion) throws SQLException {
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
	public void testMerge() throws SQLException {
		final RecursiveObject category1 = new RecursiveObject();
		category1.setName("Name1");
		final RecursiveObject category2 = new RecursiveObject();
		category2.setName("Name2");

		final ReferenceList gallery = new ReferenceList();
		gallery.setRecursiveObjects(Arrays.asList(category1, category2));
		insert(gallery);

		final RecursiveObject category3 = new RecursiveObject();
		category3.setName("Name3");

		// Prepare 1st merge
		final RecursiveObject category1ref = new RecursiveObject();
		category1ref.setId(category1.getId());
		category1ref.setName("Name1-1");
		gallery.setRecursiveObjects(Arrays.asList(category1ref, category2, category3));

		merge(gallery);
		final UUID id = gallery.getId();
		final ReferenceList selectedGallery1 = select(ReferenceList.class).byId(id);

		assertEquals(3, selectedGallery1.getRecursiveObjects().size());
		final Object[] categoryNames = selectedGallery1.getRecursiveObjects().stream().map(RecursiveObject::getName)
				.sorted().toArray();
		assertArrayEquals(new String[] { "Name1-1", "Name2", "Name3" }, categoryNames);

		// Prepare 2nd merge
		gallery.setRecursiveObjects(Arrays.asList(category2));
		merge(gallery);
		final ReferenceList selectedGallery2 = select(ReferenceList.class).byId(id);
		assertEquals(1, selectedGallery2.getRecursiveObjects().size());
		assertEquals(category2.getId(), selectedGallery2.getRecursiveObjects().get(0).getId());

	}

	@Test
	public void testMergeForInsert() throws SQLException {
		final ReferenceList referenceList = new ReferenceList();
		merge(referenceList);

		final List<ReferenceList> selectedElements = select(ReferenceList.class).execute();
		assertEquals(1, selectedElements.size());
	}

	@Test
	public void testMergeInsertList() throws SQLException {
		final ReferenceList referenceList = new ReferenceList();
		final RecursiveObject recursiveObject1 = new RecursiveObject();
		final RecursiveObject recursiveObject2 = new RecursiveObject();
		referenceList.setRecursiveObjects(Arrays.asList(recursiveObject1, recursiveObject2));
		merge(referenceList);

		final List<RecursiveObject> selectedElements = select(RecursiveObject.class).execute();
		assertEquals(2, selectedElements.size());
	}
}
