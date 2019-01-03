package de.minee.jpa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.minee.datamodel.ArrayTypes;
import de.minee.datamodel.Category;
import de.minee.datamodel.PictureContent;
import de.minee.datamodel.PrimitiveList;
import de.minee.datamodel.enumeration.Encryption;
import de.minee.datamodel.update.Gallery;
import de.minee.datamodel.update.Picture;
import de.minee.datamodel.update.User;

public class AbstractDAOTest extends AbstractDAO {

	private static Connection connection;

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
		createTable(Category.class);
		createTable(de.minee.datamodel.Gallery.class);
		createTable(de.minee.datamodel.Picture.class);
		createTable(de.minee.datamodel.User.class);
		createTable(PictureContent.class);
		createTable(PrimitiveList.class);
		createTable(ArrayTypes.class);
		updateTable(User.class);
		updateTable(Picture.class, true);
		updateTable(Gallery.class);
		return 1;
	}

	@BeforeClass
	public static void init() throws SQLException, ClassNotFoundException {
		Class.forName("org.h2.Driver");
		connection = DriverManager.getConnection("jdbc:h2:mem:", "", "streng_geheim");

	}

	@Test(expected = SQLException.class)
	public void testCreateTableFor() throws SQLException {
		createTable(Category.class);
	}

	@Test(expected = SQLException.class)
	public void testDeletionOnUpdateTable() throws SQLException {
		final de.minee.datamodel.Picture picture = new de.minee.datamodel.Picture();
		picture.setId(UUID.randomUUID());
		picture.setDescription("abc");
		insertShallow(picture);
	}

	@Test
	public void testEnum() throws SQLException {
		final PictureContent pictureContent = new PictureContent();
		pictureContent.setEncryption(Encryption.AES);

		final UUID id = insert(pictureContent);

		Assert.assertNotNull(id);

		final PictureContent selectedPictureContent = select(PictureContent.class).byId(id);

		Assert.assertNotNull(selectedPictureContent);
		Assert.assertEquals(pictureContent.getId(), selectedPictureContent.getId());
		Assert.assertEquals(pictureContent.getEncryption(), selectedPictureContent.getEncryption());
	}

	@Test
	public void testSelectEnum() throws SQLException {
		final PictureContent pictureContent1 = new PictureContent();
		pictureContent1.setEncryption(Encryption.AES);
		final PictureContent pictureContent2 = new PictureContent();
		pictureContent2.setEncryption(Encryption.PLAIN);
		final PictureContent pictureContent3 = new PictureContent();
		pictureContent3.setEncryption(Encryption.AES);

		insert(pictureContent1);
		insert(pictureContent2);
		insert(pictureContent3);

		final List<PictureContent> selectedPictureContent1 = select(PictureContent.class)
				.where(PictureContent::getEncryption).is(Encryption.AES).execute();

		Assert.assertNotNull(selectedPictureContent1);
		Assert.assertEquals(2, selectedPictureContent1.size());

		final List<PictureContent> selectedPictureContent2 = select(PictureContent.class).execute();

		Assert.assertNotNull(selectedPictureContent2);
		Assert.assertEquals(3, selectedPictureContent2.size());

	}

	@Test
	public void testList() throws SQLException {
		final List<Category> categories = new ArrayList<>();
		final Category category1 = new Category();
		final Category category2 = new Category();
		final Category category3 = new Category();
		category1.setId(UUID.randomUUID());
		category2.setId(UUID.randomUUID());
		category3.setId(UUID.randomUUID());

		categories.add(category1);
		categories.add(category2);
		final Gallery gallery = new Gallery();
		gallery.setName("gname");
		gallery.setCategories(categories);

		select(Gallery.class).where(Gallery::getCategories).is(Arrays.asList(category3)).execute();
		insert(gallery);
		final Gallery selectedGallery = select(Gallery.class).byId(gallery.getId());

		Assert.assertNotNull(selectedGallery);
		Assert.assertEquals(gallery.getId(), selectedGallery.getId());
		Assert.assertEquals(2, selectedGallery.getCategories().size());
	}

	@Test
	public void test() throws SQLException {
		final List<User> user1 = select(User.class).where(User::getName).is("ABC").execute();
		final User user = new User();
		user.setId(UUID.randomUUID());
		user.setName("Name");
		user.setAuthentication("Auth");
		final Picture picture = new Picture();
		picture.setId(UUID.randomUUID());
		user.setPicture(picture);
		insertShallow(user);
		System.out.println(user);
		final List<User> user2 = select(User.class).execute();
		System.out.println(user2.get(0));
		final List<User> user3 = select(User.class).where(User::getName).is("ABC").execute();
		System.out.println(user3);
		final List<User> user4 = select(User.class).where(User::getName).is("Name").execute();
		System.out.println(user4);
		final List<User> user5 = select(User.class).where(User::getName).in("Name").execute();
		System.out.println(user5);
		final User userUpdate = new User();
		final UUID id = UUID.randomUUID();
		userUpdate.setId(id);
		userUpdate.setName("mail");
		userUpdate.setEmail("a@b.c");
		insertShallow(userUpdate);
		final User user6 = select(User.class).byId(id);
		System.out.println(user6);

	}

	@Test
	public void testUpdatedLists() throws SQLException {

		final Category category = new Category();
		category.setId(UUID.randomUUID());
		final List<Category> categories = Arrays.asList(category);

		final Picture picture = new Picture();
		picture.setId(UUID.randomUUID());
		final List<Picture> pictures = Arrays.asList(picture);

		final Gallery gallery = new Gallery();
		final UUID galleryId = UUID.randomUUID();
		gallery.setId(galleryId);
		gallery.setCategories(categories);
		gallery.setPictures(pictures);

		final Gallery selectedGallery1 = select(Gallery.class).byId(galleryId);
		insert(gallery);
		final Gallery selectedGallery2 = select(Gallery.class).byId(galleryId);

		Assert.assertNull(selectedGallery1);
		Assert.assertNotNull(selectedGallery2);
		Assert.assertEquals(gallery.getId(), selectedGallery2.getId());
		Assert.assertEquals(1, selectedGallery2.getCategories().size());
		Assert.assertEquals(1, selectedGallery2.getPictures().size());

	}

	@Test
	public void testShallowInsertList() throws SQLException {
		final Gallery gallery = new Gallery();
		gallery.setCategories(Arrays.asList(new Category()));

		Assert.assertNull(gallery.getId());
		Assert.assertNull(gallery.getCategories().get(0).getId());
		insertShallow(gallery);
		Assert.assertNotNull(gallery.getId());
		Assert.assertNull(gallery.getCategories().get(0).getId());

		final Gallery selectedGallery = select(Gallery.class).byId(gallery.getId());
		Assert.assertNotNull(selectedGallery);
		Assert.assertEquals(0, selectedGallery.getCategories().size());

	}

	@Test
	public void testShallowInsertListWithId() throws SQLException {
		final Gallery gallery = new Gallery();
		final Category category = new Category();
		category.setId(UUID.randomUUID());
		gallery.setCategories(Arrays.asList(category));

		Assert.assertNull(gallery.getId());
		Assert.assertNotNull(gallery.getCategories().get(0).getId());
		insertShallow(gallery);
		Assert.assertNotNull(gallery.getId());
		Assert.assertNotNull(gallery.getCategories().get(0).getId());

		final Gallery selectedGallery = select(Gallery.class).byId(gallery.getId());
		Assert.assertNotNull(selectedGallery);
		Assert.assertEquals(1, selectedGallery.getCategories().size());
		Assert.assertNull(selectedGallery.getCategories().get(0));

	}

	@Test
	public void testShallowInsertListWithSeperateInsertedListElement() throws SQLException {
		final Gallery gallery = new Gallery();
		final Category category = new Category();
		insertShallow(category);
		gallery.setCategories(Arrays.asList(category));

		Assert.assertNull(gallery.getId());
		Assert.assertNotNull(gallery.getCategories().get(0).getId());
		insertShallow(gallery);
		Assert.assertNotNull(gallery.getId());
		Assert.assertNotNull(gallery.getCategories().get(0).getId());

		final Gallery selectedGallery = select(Gallery.class).byId(gallery.getId());
		Assert.assertNotNull(selectedGallery);
		Assert.assertEquals(1, selectedGallery.getCategories().size());
		Assert.assertNotNull(selectedGallery.getCategories().get(0));

	}

	@Test
	public void testShallowInsertChild() throws SQLException {
		final User user = new User();
		user.setPicture(new Picture());

		Assert.assertNull(user.getId());
		Assert.assertNull(user.getPicture().getId());
		insertShallow(user);
		Assert.assertNotNull(user.getId());
		Assert.assertNull(user.getPicture().getId());
		final User selectedUser = select(User.class).byId(user.getId());
		Assert.assertNotNull(selectedUser);
		Assert.assertNull(selectedUser.getPicture());

	}

	@Test
	public void testAutoSetId() throws SQLException {

		// Direct setting of Id into the Object
		final Picture picture = new Picture();

		Assert.assertNull(picture.getId());
		insertShallow(picture);
		Assert.assertNotNull(picture.getId());

		// Setting indirect Ids into List childs
		final Gallery gallery = new Gallery();
		gallery.setCategories(Arrays.asList(new Category()));

		Assert.assertNull(gallery.getId());
		Assert.assertNull(gallery.getCategories().get(0).getId());
		insert(gallery);
		Assert.assertNotNull(gallery.getId());
		Assert.assertNotNull(gallery.getCategories().get(0).getId());

		// Setting indirect Ids into childs
		final User user = new User();
		user.setPicture(new Picture());

		Assert.assertNull(user.getId());
		Assert.assertNull(user.getPicture().getId());
		insert(user);
		Assert.assertNotNull(user.getId());
		Assert.assertNotNull(user.getPicture().getId());

		final User selectedUser = select(User.class).byId(user.getId());
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
}
