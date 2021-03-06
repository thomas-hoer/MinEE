package de.minee.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import de.minee.datamodel.ArrayTypes;
import de.minee.datamodel.DateType;
import de.minee.datamodel.EnumObject;
import de.minee.datamodel.NotSupportedType;
import de.minee.datamodel.PrimitiveList;
import de.minee.datamodel.PrimitiveObjects;
import de.minee.datamodel.PrimitiveTypes;
import de.minee.datamodel.RecursiveObject;
import de.minee.datamodel.enumeration.Enumeration;
import de.minee.datamodel.invalid.FieldPropertyMismatch;
import de.minee.datamodel.invalid.NonDefaultConstructor;
import de.minee.datamodel.update.ReferenceChain;
import de.minee.datamodel.update.ReferenceList;
import de.minee.datamodel.update.SimpleReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class AbstractDAOTest extends AbstractTestDAO {

	@Override
	protected int updateDatabaseSchema(final int oldDbSchemaVersion) {
		createTable(RecursiveObject.class);
		createTable(de.minee.datamodel.ReferenceList.class);
		createTable(de.minee.datamodel.SimpleReference.class);
		createTable(de.minee.datamodel.ReferenceChain.class);
		createTable(EnumObject.class);
		createTable(PrimitiveList.class);
		createTable(ArrayTypes.class);
		createTable(NonDefaultConstructor.class);
		createTable(FieldPropertyMismatch.class);
		createTable(PrimitiveObjects.class);
		createTable(PrimitiveTypes.class);
		updateTable(ReferenceChain.class);
		updateTable(SimpleReference.class, true);
		updateTable(ReferenceList.class);
		dropTable(EnumObject.class);
		createTable(EnumObject.class);
		return 1;
	}

	@Test(expected = DatabaseException.class)
	public void testCreateTableFor() {
		createTable(RecursiveObject.class);
	}

	@Test(expected = DatabaseException.class)
	public void testDeletionOnUpdateTable() {
		final de.minee.datamodel.SimpleReference picture = new de.minee.datamodel.SimpleReference();
		picture.setId(UUID.randomUUID());
		picture.setName("abc");
		insertShallow(picture);
	}

	@Test
	public void testEnum() {
		final EnumObject enumObject = new EnumObject();
		enumObject.setEnumeration(Enumeration.ENUM_VALUE_1);

		final UUID id = insert(enumObject);

		Assert.assertNotNull(id);

		final EnumObject selectedEnumObject = select(EnumObject.class).byId(id);

		Assert.assertNotNull(selectedEnumObject);
		Assert.assertEquals(enumObject.getId(), selectedEnumObject.getId());
		Assert.assertEquals(enumObject.getEnumeration(), selectedEnumObject.getEnumeration());
	}

	@Test
	public void testEnumList() {
		final EnumObject enumObject = new EnumObject();
		enumObject.setEnumList(Arrays.asList(Enumeration.ENUM_VALUE_1, Enumeration.ENUM_VALUE_2));

		final UUID id = insert(enumObject);

		Assert.assertNotNull(id);

		final EnumObject selectedEnumObject = select(EnumObject.class).byId(id);

		Assert.assertNotNull(selectedEnumObject);
		Assert.assertEquals(enumObject.getId(), selectedEnumObject.getId());
		Assert.assertEquals(2, selectedEnumObject.getEnumList().size());
		Assert.assertTrue(selectedEnumObject.getEnumList().contains(Enumeration.ENUM_VALUE_1));
		Assert.assertTrue(selectedEnumObject.getEnumList().contains(Enumeration.ENUM_VALUE_2));

		// Delete ENUM_VALUE_1 Add ENUM_VALUE_3
		enumObject.setEnumList(Arrays.asList(Enumeration.ENUM_VALUE_2, Enumeration.ENUM_VALUE_3));
		merge(enumObject);

		final EnumObject selectedEnumObject2 = select(EnumObject.class).byId(id);

		Assert.assertNotNull(selectedEnumObject2);
		Assert.assertEquals(enumObject.getId(), selectedEnumObject2.getId());
		Assert.assertEquals(2, selectedEnumObject2.getEnumList().size());
		Assert.assertTrue(selectedEnumObject2.getEnumList().contains(Enumeration.ENUM_VALUE_2));
		Assert.assertTrue(selectedEnumObject2.getEnumList().contains(Enumeration.ENUM_VALUE_3));

		enumObject.setEnumList(Arrays.asList(Enumeration.ENUM_VALUE_4, Enumeration.ENUM_VALUE_5));
		update(enumObject);

		final EnumObject selectedEnumObject3 = select(EnumObject.class).byId(id);

		Assert.assertNotNull(selectedEnumObject3);
		Assert.assertEquals(enumObject.getId(), selectedEnumObject3.getId());
		Assert.assertEquals(2, selectedEnumObject3.getEnumList().size());
		Assert.assertTrue(selectedEnumObject3.getEnumList().contains(Enumeration.ENUM_VALUE_4));
		Assert.assertTrue(selectedEnumObject3.getEnumList().contains(Enumeration.ENUM_VALUE_5));
	}

	@Test
	public void testCycle() {
		final RecursiveObject recursiveObject = new RecursiveObject();
		recursiveObject.setChild(recursiveObject);

		final UUID id = insert(recursiveObject);

		final RecursiveObject selectedObject = select(RecursiveObject.class).byId(id);

		Assert.assertNotNull(selectedObject);
		Assert.assertEquals(selectedObject, selectedObject.getChild());
	}

	@Test
	public void testSelectEnum() {
		final EnumObject enumObject1 = new EnumObject();
		enumObject1.setEnumeration(Enumeration.ENUM_VALUE_1);
		final EnumObject enumObject2 = new EnumObject();
		enumObject2.setEnumeration(Enumeration.ENUM_VALUE_2);
		final EnumObject enumObject3 = new EnumObject();
		enumObject3.setEnumeration(Enumeration.ENUM_VALUE_1);

		insert(enumObject1);
		insert(enumObject2);
		insert(enumObject3);

		final List<EnumObject> selectedEnumObject1 = select(EnumObject.class).where(EnumObject::getEnumeration)
				.is(Enumeration.ENUM_VALUE_1).execute();

		Assert.assertNotNull(selectedEnumObject1);
		Assert.assertEquals(2, selectedEnumObject1.size());

		final List<EnumObject> selectedEnumObject2 = select(EnumObject.class).execute();

		Assert.assertNotNull(selectedEnumObject2);
		Assert.assertEquals(3, selectedEnumObject2.size());

	}

	@Test
	public void testList() {
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
	public void testListWithNullElement() {
		final List<RecursiveObject> recursiveObjects = new ArrayList<>();
		final RecursiveObject recursiveObject = new RecursiveObject();
		recursiveObject.setId(UUID.randomUUID());

		recursiveObjects.add(recursiveObject);
		recursiveObjects.add(null);
		final ReferenceList referenceList = new ReferenceList();
		referenceList.setRecursiveObjects(recursiveObjects);

		insert(referenceList);
		final ReferenceList selectedReferenceList = select(ReferenceList.class).byId(referenceList.getId());

		Assert.assertNotNull(selectedReferenceList);
		Assert.assertEquals(referenceList.getId(), selectedReferenceList.getId());
		Assert.assertEquals(1, selectedReferenceList.getRecursiveObjects().size());
	}

	@Test
	public void testUpdatedLists() {

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
	public void testShallowInsertList() {
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
	public void testShallowInsertListWithId() {
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
	public void testShallowInsertListWithSeperateInsertedListElement() {
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
	public void testShallowInsertChild() {
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
	public void testSelectNull() {
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
	public void testSelectIsNot() {
		final EnumObject enumObject1 = new EnumObject();
		enumObject1.setString("abc");
		final EnumObject enumObject2 = new EnumObject();
		enumObject2.setString("def");
		final EnumObject enumObject3 = new EnumObject();
		enumObject3.setString("ghi");

		insert(enumObject1);
		insert(enumObject2);
		insert(enumObject3);

		final List<EnumObject> selectedResources = select(EnumObject.class).where(EnumObject::getString).isNot("def")
				.execute();

		assertEquals(2, selectedResources.size());
		assertEquals("abc", selectedResources.get(0).getString());
		assertEquals("ghi", selectedResources.get(1).getString());
	}

	@Test
	public void testAutoSetId() {

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
	public void testPrimitiveList() {
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
	public void testArray() {
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

	@Test
	public void testSelectPrimitives() {
		final PrimitiveTypes primitiveTypes = new PrimitiveTypes();
		primitiveTypes.setBoolValue(true);
		primitiveTypes.setByteValue((byte) 42);
		primitiveTypes.setCharValue('=');
		primitiveTypes.setDoubleValue(2e100);
		primitiveTypes.setFloatValue(5.2e12f);
		primitiveTypes.setIntValue(987654321);
		primitiveTypes.setLongValue(9999999999l);
		primitiveTypes.setShortValue((short) 1000);
		insert(primitiveTypes);
		List<PrimitiveTypes> selectedResources = null;
		selectedResources = select(PrimitiveTypes.class).where(PrimitiveTypes::getBoolValue).is(true).execute();
		assertEquals(1, selectedResources.size());
		assertEquals(primitiveTypes.getId(), selectedResources.get(0).getId());
		selectedResources = select(PrimitiveTypes.class).where(PrimitiveTypes::getByteValue).is((byte) 42).execute();
		assertEquals(1, selectedResources.size());
		assertEquals(primitiveTypes.getId(), selectedResources.get(0).getId());
		selectedResources = select(PrimitiveTypes.class).where(PrimitiveTypes::getCharValue).is('=').execute();
		assertEquals(1, selectedResources.size());
		assertEquals(primitiveTypes.getId(), selectedResources.get(0).getId());
		selectedResources = select(PrimitiveTypes.class).where(PrimitiveTypes::getDoubleValue).is(2e100).execute();
		assertEquals(1, selectedResources.size());
		assertEquals(primitiveTypes.getId(), selectedResources.get(0).getId());
		selectedResources = select(PrimitiveTypes.class).where(PrimitiveTypes::getFloatValue).is(5.2e12f).execute();
		assertEquals(1, selectedResources.size());
		assertEquals(primitiveTypes.getId(), selectedResources.get(0).getId());
		selectedResources = select(PrimitiveTypes.class).where(PrimitiveTypes::getIntValue).is(987654321).execute();
		assertEquals(1, selectedResources.size());
		assertEquals(primitiveTypes.getId(), selectedResources.get(0).getId());
		selectedResources = select(PrimitiveTypes.class).where(PrimitiveTypes::getLongValue).is(9999999999l).execute();
		assertEquals(1, selectedResources.size());
		assertEquals(primitiveTypes.getId(), selectedResources.get(0).getId());
		selectedResources = select(PrimitiveTypes.class).where(PrimitiveTypes::getShortValue).is((short) 1000)
				.execute();
		assertEquals(1, selectedResources.size());
		assertEquals(primitiveTypes.getId(), selectedResources.get(0).getId());
	}

	@Test
	public void testSelectPrimitiveWrapper() {
		final PrimitiveObjects primitiveObjects = new PrimitiveObjects();
		primitiveObjects.setBoolValue(true);
		primitiveObjects.setByteValue((byte) 42);
		primitiveObjects.setCharValue('=');
		primitiveObjects.setDoubleValue(2e100);
		primitiveObjects.setFloatValue(5.2e12f);
		primitiveObjects.setIntValue(987654321);
		primitiveObjects.setLongValue(9999999999l);
		primitiveObjects.setShortValue((short) 1000);
		insert(primitiveObjects);
		List<PrimitiveObjects> selectedResources = null;
		selectedResources = select(PrimitiveObjects.class).where(PrimitiveObjects::getBoolValue).is(true).execute();
		assertEquals(1, selectedResources.size());
		assertEquals(primitiveObjects.getId(), selectedResources.get(0).getId());
		selectedResources = select(PrimitiveObjects.class).where(PrimitiveObjects::getByteValue).is((byte) 42)
				.execute();
		assertEquals(1, selectedResources.size());
		assertEquals(primitiveObjects.getId(), selectedResources.get(0).getId());
		selectedResources = select(PrimitiveObjects.class).where(PrimitiveObjects::getCharValue).is('=').execute();
		assertEquals(1, selectedResources.size());
		assertEquals(primitiveObjects.getId(), selectedResources.get(0).getId());
		selectedResources = select(PrimitiveObjects.class).where(PrimitiveObjects::getDoubleValue).is(2e100).execute();
		assertEquals(1, selectedResources.size());
		assertEquals(primitiveObjects.getId(), selectedResources.get(0).getId());
		selectedResources = select(PrimitiveObjects.class).where(PrimitiveObjects::getFloatValue).is(5.2e12f).execute();
		assertEquals(1, selectedResources.size());
		assertEquals(primitiveObjects.getId(), selectedResources.get(0).getId());
		selectedResources = select(PrimitiveObjects.class).where(PrimitiveObjects::getIntValue).is(987654321).execute();
		assertEquals(1, selectedResources.size());
		assertEquals(primitiveObjects.getId(), selectedResources.get(0).getId());
		selectedResources = select(PrimitiveObjects.class).where(PrimitiveObjects::getLongValue).is(9999999999l)
				.execute();
		assertEquals(1, selectedResources.size());
		assertEquals(primitiveObjects.getId(), selectedResources.get(0).getId());
		selectedResources = select(PrimitiveObjects.class).where(PrimitiveObjects::getShortValue).is((short) 1000)
				.execute();
		assertEquals(1, selectedResources.size());
		assertEquals(primitiveObjects.getId(), selectedResources.get(0).getId());
	}

	@Test(expected = MappingException.class)
	public void testUnsupportedType() {
		final AbstractDAO dao = new AbstractTestDAO() {

			@Override
			protected int updateDatabaseSchema(final int oldDbSchemaVersion) {
				createTable(NotSupportedType.class);
				return 0;
			}

		};
		dao.select(NotSupportedType.class).execute();
	}

	@Test(expected = DatabaseException.class)
	public void testDropTable() {
		dropTable(SimpleReference.class);
	}

	@Test(expected = DatabaseException.class)
	public void testUpdateTable() {
		updateTable(SimpleReference.class);
	}

	@Test(expected = DatabaseException.class)
	public void testUnsuportedType() {
		select(NotSupportedType.class).execute();
	}

	@Test(expected = DatabaseException.class)
	public void testDateType() {
		final DateType object = new DateType();
		object.setDate(new Date());
		insert(object);
		select(DateType.class).execute();
	}

	@Test(expected = DatabaseException.class)
	public void testFieldPropertyMismatch() {
		final FieldPropertyMismatch object = new FieldPropertyMismatch();
		object.setBool(true);
		insert(object);
		final List<FieldPropertyMismatch> list = select(FieldPropertyMismatch.class)
				.where(FieldPropertyMismatch::getBool).is().execute(Arrays.asList(true));
	}

	@Test(expected = DatabaseException.class)
	public void testSelectWithNotSupportedDatatype() {
		final NonDefaultConstructor object = new NonDefaultConstructor(UUID.randomUUID());
		insert(object);
		select(NonDefaultConstructor.class).execute();
	}

	@Test(expected = DatabaseException.class)
	public void testSelectWithNotSupportedDatatype2() {
		final UUID randomUUID = UUID.randomUUID();
		final NonDefaultConstructor object = new NonDefaultConstructor(randomUUID);
		insert(object);
		select(NonDefaultConstructor.class).where(NonDefaultConstructor::getId).is().execute(Arrays.asList(randomUUID));
	}

	@Test(expected = DatabaseException.class)
	public void testSelectWithNotSupportedDatatypeById() {
		final UUID randomUUID = UUID.randomUUID();
		final NonDefaultConstructor object = new NonDefaultConstructor(randomUUID);
		insert(object);
		select(NonDefaultConstructor.class).byId(randomUUID);
	}
}
