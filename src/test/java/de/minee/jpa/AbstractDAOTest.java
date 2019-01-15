package de.minee.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import de.minee.datamodel.ArrayTypes;
import de.minee.datamodel.EnumObject;
import de.minee.datamodel.PrimitiveList;
import de.minee.datamodel.RecursiveObject;
import de.minee.datamodel.enumeration.Enumeration;
import de.minee.datamodel.update.ReferenceChain;
import de.minee.datamodel.update.ReferenceList;
import de.minee.datamodel.update.SimpleReference;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class AbstractDAOTest extends AbstractTestDAO {

	@Override
	protected int updateDatabaseSchema(final int oldDbSchemaVersion) throws SQLException {
		createTable(RecursiveObject.class);
		createTable(de.minee.datamodel.ReferenceList.class);
		createTable(de.minee.datamodel.SimpleReference.class);
		createTable(de.minee.datamodel.ReferenceChain.class);
		createTable(EnumObject.class);
		createTable(PrimitiveList.class);
		createTable(ArrayTypes.class);
		updateTable(ReferenceChain.class);
		updateTable(SimpleReference.class, true);
		updateTable(ReferenceList.class);
		dropTable(EnumObject.class);
		createTable(EnumObject.class);
		return 1;
	}

	@Test(expected = SQLException.class)
	public void testCreateTableFor() throws SQLException {
		createTable(RecursiveObject.class);
	}

	@Test(expected = SQLException.class)
	public void testDeletionOnUpdateTable() throws SQLException {
		final de.minee.datamodel.SimpleReference picture = new de.minee.datamodel.SimpleReference();
		picture.setId(UUID.randomUUID());
		picture.setName("abc");
		insertShallow(picture);
	}

	@Test
	public void testEnum() throws SQLException {
		final EnumObject pictureContent = new EnumObject();
		pictureContent.setEnumeration(Enumeration.ENUM_VALUE_1);

		final UUID id = insert(pictureContent);

		Assert.assertNotNull(id);

		final EnumObject selectedPictureContent = select(EnumObject.class).byId(id);

		Assert.assertNotNull(selectedPictureContent);
		Assert.assertEquals(pictureContent.getId(), selectedPictureContent.getId());
		Assert.assertEquals(pictureContent.getEnumeration(), selectedPictureContent.getEnumeration());
	}

	@Test
	public void testCycle() throws SQLException {
		final RecursiveObject recursiveObject = new RecursiveObject();
		recursiveObject.setChild(recursiveObject);

		final UUID id = insert(recursiveObject);

		final RecursiveObject selectedObject = select(RecursiveObject.class).byId(id);

		Assert.assertNotNull(selectedObject);
		Assert.assertEquals(selectedObject, selectedObject.getChild());
	}

	@Test
	public void testSelectEnum() throws SQLException {
		final EnumObject pictureContent1 = new EnumObject();
		pictureContent1.setEnumeration(Enumeration.ENUM_VALUE_1);
		final EnumObject pictureContent2 = new EnumObject();
		pictureContent2.setEnumeration(Enumeration.ENUM_VALUE_2);
		final EnumObject pictureContent3 = new EnumObject();
		pictureContent3.setEnumeration(Enumeration.ENUM_VALUE_1);

		insert(pictureContent1);
		insert(pictureContent2);
		insert(pictureContent3);

		final List<EnumObject> selectedPictureContent1 = select(EnumObject.class).where(EnumObject::getEnumeration)
				.is(Enumeration.ENUM_VALUE_1).execute();

		Assert.assertNotNull(selectedPictureContent1);
		Assert.assertEquals(2, selectedPictureContent1.size());

		final List<EnumObject> selectedPictureContent2 = select(EnumObject.class).execute();

		Assert.assertNotNull(selectedPictureContent2);
		Assert.assertEquals(3, selectedPictureContent2.size());

	}

	@Test
	public void testList() throws SQLException {
		final List<RecursiveObject> categories = new ArrayList<>();
		final RecursiveObject category1 = new RecursiveObject();
		final RecursiveObject category2 = new RecursiveObject();
		final RecursiveObject category3 = new RecursiveObject();
		category1.setId(UUID.randomUUID());
		category2.setId(UUID.randomUUID());
		category3.setId(UUID.randomUUID());

		categories.add(category1);
		categories.add(category2);
		final ReferenceList gallery = new ReferenceList();
		gallery.setName("gname");
		gallery.setRecursiveObjects(categories);

		select(ReferenceList.class).where(ReferenceList::getRecursiveObjects).is(Arrays.asList(category3)).execute();
		insert(gallery);
		final ReferenceList selectedGallery = select(ReferenceList.class).byId(gallery.getId());

		Assert.assertNotNull(selectedGallery);
		Assert.assertEquals(gallery.getId(), selectedGallery.getId());
		Assert.assertEquals(2, selectedGallery.getRecursiveObjects().size());
	}

	@Test
	public void testUpdatedLists() throws SQLException {

		final RecursiveObject category = new RecursiveObject();
		category.setId(UUID.randomUUID());
		final List<RecursiveObject> categories = Arrays.asList(category);

		final SimpleReference picture = new SimpleReference();
		picture.setId(UUID.randomUUID());
		final List<SimpleReference> pictures = Arrays.asList(picture);

		final ReferenceList gallery = new ReferenceList();
		final UUID galleryId = UUID.randomUUID();
		gallery.setId(galleryId);
		gallery.setRecursiveObjects(categories);
		gallery.setPictures(pictures);

		final ReferenceList selectedGallery1 = select(ReferenceList.class).byId(galleryId);
		insert(gallery);
		final ReferenceList selectedGallery2 = select(ReferenceList.class).byId(galleryId);

		Assert.assertNull(selectedGallery1);
		Assert.assertNotNull(selectedGallery2);
		Assert.assertEquals(gallery.getId(), selectedGallery2.getId());
		Assert.assertEquals(1, selectedGallery2.getRecursiveObjects().size());
		Assert.assertEquals(1, selectedGallery2.getPictures().size());

	}

	@Test
	public void testShallowInsertList() throws SQLException {
		final ReferenceList gallery = new ReferenceList();
		gallery.setRecursiveObjects(Arrays.asList(new RecursiveObject()));

		Assert.assertNull(gallery.getId());
		Assert.assertNull(gallery.getRecursiveObjects().get(0).getId());
		insertShallow(gallery);
		Assert.assertNotNull(gallery.getId());
		Assert.assertNull(gallery.getRecursiveObjects().get(0).getId());

		final ReferenceList selectedGallery = select(ReferenceList.class).byId(gallery.getId());
		Assert.assertNotNull(selectedGallery);
		Assert.assertEquals(0, selectedGallery.getRecursiveObjects().size());

	}

	@Test
	public void testShallowInsertListWithId() throws SQLException {
		final ReferenceList gallery = new ReferenceList();
		final RecursiveObject category = new RecursiveObject();
		category.setId(UUID.randomUUID());
		gallery.setRecursiveObjects(Arrays.asList(category));

		Assert.assertNull(gallery.getId());
		Assert.assertNotNull(gallery.getRecursiveObjects().get(0).getId());
		insertShallow(gallery);
		Assert.assertNotNull(gallery.getId());
		Assert.assertNotNull(gallery.getRecursiveObjects().get(0).getId());

		final ReferenceList selectedGallery = select(ReferenceList.class).byId(gallery.getId());
		Assert.assertNotNull(selectedGallery);
		Assert.assertEquals(1, selectedGallery.getRecursiveObjects().size());
		Assert.assertNull(selectedGallery.getRecursiveObjects().get(0));

	}

	@Test
	public void testShallowInsertListWithSeperateInsertedListElement() throws SQLException {
		final ReferenceList gallery = new ReferenceList();
		final RecursiveObject category = new RecursiveObject();
		insertShallow(category);
		gallery.setRecursiveObjects(Arrays.asList(category));

		Assert.assertNull(gallery.getId());
		Assert.assertNotNull(gallery.getRecursiveObjects().get(0).getId());
		insertShallow(gallery);
		Assert.assertNotNull(gallery.getId());
		Assert.assertNotNull(gallery.getRecursiveObjects().get(0).getId());

		final ReferenceList selectedGallery = select(ReferenceList.class).byId(gallery.getId());
		Assert.assertNotNull(selectedGallery);
		Assert.assertEquals(1, selectedGallery.getRecursiveObjects().size());
		Assert.assertNotNull(selectedGallery.getRecursiveObjects().get(0));

	}

	@Test
	public void testShallowInsertChild() throws SQLException {
		final ReferenceChain referenceChain = new ReferenceChain();
		referenceChain.setPicture(new SimpleReference());

		Assert.assertNull(referenceChain.getId());
		Assert.assertNull(referenceChain.getPicture().getId());
		insertShallow(referenceChain);
		Assert.assertNotNull(referenceChain.getId());
		Assert.assertNull(referenceChain.getPicture().getId());
		final ReferenceChain selectedUser = select(ReferenceChain.class).byId(referenceChain.getId());
		Assert.assertNotNull(selectedUser);
		Assert.assertNull(selectedUser.getPicture());

	}

	@Test
	public void testSelectNull() throws SQLException {
		final ReferenceChain referenceChain = new ReferenceChain();
		referenceChain.setEmail("email");
		insert(referenceChain);
		final List<ReferenceChain> selectedResources = select(ReferenceChain.class).where(ReferenceChain::getName)
				.isNull().execute();

		assertEquals(1, selectedResources.size());
		assertNull(selectedResources.get(0).getName());
		assertEquals("email", selectedResources.get(0).getEmail());
	}

	@Test
	public void testAutoSetId() throws SQLException {

		// Direct setting of Id into the Object
		final SimpleReference picture = new SimpleReference();

		Assert.assertNull(picture.getId());
		insertShallow(picture);
		Assert.assertNotNull(picture.getId());

		// Setting indirect Ids into List childs
		final ReferenceList gallery = new ReferenceList();
		gallery.setRecursiveObjects(Arrays.asList(new RecursiveObject()));

		Assert.assertNull(gallery.getId());
		Assert.assertNull(gallery.getRecursiveObjects().get(0).getId());
		insert(gallery);
		Assert.assertNotNull(gallery.getId());
		Assert.assertNotNull(gallery.getRecursiveObjects().get(0).getId());

		// Setting indirect Ids into childs
		final ReferenceChain user = new ReferenceChain();
		user.setPicture(new SimpleReference());

		Assert.assertNull(user.getId());
		Assert.assertNull(user.getPicture().getId());
		insert(user);
		Assert.assertNotNull(user.getId());
		Assert.assertNotNull(user.getPicture().getId());

		final ReferenceChain selectedUser = select(ReferenceChain.class).byId(user.getId());
		Assert.assertNotNull(selectedUser.getPicture());
	}

	@Test
	public void testPrimitiveList() throws SQLException {
		final PrimitiveList primitiveList = new PrimitiveList();
		primitiveList.setBoolList(Arrays.asList(true, false, true));

		final UUID id = insert(primitiveList);

		final PrimitiveList selectedPrimitiveList = select(PrimitiveList.class).byId(id);

		Assert.assertNotNull(id);
		Assert.assertNotNull(selectedPrimitiveList);
		final List<Boolean> boolList = selectedPrimitiveList.getBoolList();
		Assert.assertNotNull(boolList);
		Assert.assertEquals(3, boolList.size());
		Assert.assertTrue(boolList.get(0));
		Assert.assertFalse(boolList.get(1));
		Assert.assertTrue(boolList.get(2));
	}

	@Test
	public void testArray() throws SQLException {
		final ArrayTypes arrayTypes = new ArrayTypes();
		arrayTypes.setByteArray(new byte[] { 1, -1, 10, 99, 44 });
		arrayTypes.setIntArray(new Integer[] { 1, 2, 10, 10, 11, 0 });

		final UUID id = insert(arrayTypes);

		final ArrayTypes selectedArrayTypes = select(ArrayTypes.class).byId(id);

		Assert.assertNotNull(selectedArrayTypes);

		final byte[] byteArray = selectedArrayTypes.getByteArray();
		Assert.assertNotNull(byteArray);
		Assert.assertEquals(5, byteArray.length);
		Assert.assertArrayEquals(new byte[] { 1, -1, 10, 99, 44 }, byteArray);

		final Integer[] intArray = selectedArrayTypes.getIntArray();
		Assert.assertNotNull(intArray);
		Assert.assertEquals(6, intArray.length);
		Assert.assertArrayEquals(new Integer[] { 1, 2, 10, 10, 11, 0 }, intArray);
	}

	@Test(expected = SQLException.class)
	public void testDropTable() throws SQLException {
		dropTable(SimpleReference.class);
	}
}
