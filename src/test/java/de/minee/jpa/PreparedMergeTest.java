package de.minee.jpa;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import de.minee.datamodel.ArrayTypes;
import de.minee.datamodel.RecursiveObject;
import de.minee.datamodel.ReferenceList;
import de.minee.datamodel.SimpleReference;
import de.minee.datamodel.EnumObject;
import de.minee.datamodel.PrimitiveList;
import de.minee.datamodel.ReferenceChain;

public class PreparedMergeTest extends AbstractDAO {

	@Override
	protected String getConnectionString() {
		return "jdbc:h2:mem:";
	}

	@Override
	protected String getUserName() {
		return "";
	}

	@Override
	protected String getPassword() {
		return "";
	}

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
		gallery.setCategories(Arrays.asList(category1, category2));
		insert(gallery);

		final RecursiveObject category3 = new RecursiveObject();
		category3.setName("Name3");

		// Prepare 1st merge
		final RecursiveObject category1ref = new RecursiveObject();
		category1ref.setId(category1.getId());
		category1ref.setName("Name1-1");
		gallery.setCategories(Arrays.asList(category1ref, category2, category3));

		merge(gallery);
		final UUID id = gallery.getId();
		final ReferenceList selectedGallery1 = select(ReferenceList.class).byId(id);

		Assert.assertEquals(3, selectedGallery1.getCategories().size());
		final Object[] categoryNames = selectedGallery1.getCategories().stream().map(RecursiveObject::getName).sorted()
				.toArray();
		Assert.assertArrayEquals(new String[] { "Name1-1", "Name2", "Name3" }, categoryNames);

		// Prepare 2nd merge
		gallery.setCategories(Arrays.asList(category2));
		merge(gallery);
		final ReferenceList selectedGallery2 = select(ReferenceList.class).byId(id);
		Assert.assertEquals(1, selectedGallery2.getCategories().size());
		Assert.assertEquals(category2.getId(), selectedGallery2.getCategories().get(0).getId());

	}
}
