package de.minee.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import de.minee.datamodel.RecursiveObject;
import de.minee.datamodel.ReferenceList;
import de.minee.datamodel.SimpleReference;
import de.minee.datamodel.invalid.NonDefaultConstructor;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class SelectStatementTest extends AbstractTestDAO {

	private static final String NAME_1 = "XX";
	private static final String NAME_2 = "WW";
	private static final String NAME_3 = "XY";
	private static final String VALUE_1 = "YY";
	private static final String VALUE_2 = "ZZ";
	private static final String VALUE_3 = "YZ";

	@Override
	protected int updateDatabaseSchema(final int oldDbSchemaVersion) {
		createTable(SimpleReference.class);
		createTable(RecursiveObject.class);
		createTable(ReferenceList.class);
		return 1;
	}

	/**
	 * Inserts some entries for the test cases.
	 *
	 * @throws SQLException SQLException in case of an error
	 */
	@Before
	public void init() {
		SimpleReference simpleReference;

		simpleReference = new SimpleReference();
		simpleReference.setName(NAME_1);
		simpleReference.setValue(VALUE_1);
		insert(simpleReference);

		simpleReference = new SimpleReference();
		simpleReference.setName(NAME_1);
		simpleReference.setValue(VALUE_2);
		insert(simpleReference);

		simpleReference = new SimpleReference();
		simpleReference.setName(NAME_2);
		simpleReference.setValue(VALUE_1);
		insert(simpleReference);

		simpleReference = new SimpleReference();
		simpleReference.setName(NAME_2);
		simpleReference.setValue(VALUE_2);
		insert(simpleReference);

		simpleReference = new SimpleReference();
		simpleReference.setName(NAME_3);
		simpleReference.setValue(VALUE_3);
		insert(simpleReference);
	}

	@Test
	public void testSelectAnd() {
		final List<SimpleReference> result = select(SimpleReference.class).where(SimpleReference::getName).is().and()
				.where(SimpleReference::getValue).is().execute(Arrays.asList(NAME_1, VALUE_1));

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	public void testSelectOr() {
		final List<SimpleReference> result = select(SimpleReference.class).where(SimpleReference::getName).is().or()
				.where(SimpleReference::getValue).is().execute(Arrays.asList(NAME_1, VALUE_1));

		assertNotNull(result);
		assertEquals(3, result.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyQuery() {
		select(SimpleReference.class).query("").execute();
	}

	@Test
	public void testQuery() {
		final List<SimpleReference> result = select(SimpleReference.class).query("Name = '" + NAME_1 + "'").execute();
		assertEquals(2, result.size());
	}

	@Test
	public void testSelectIn() {
		final List<SimpleReference> result = select(SimpleReference.class).where(SimpleReference::getName)
				.in(NAME_1, NAME_3).execute();

		assertNotNull(result);
		assertEquals(3, result.size());
	}

	@Test
	public void testSelectInList() {
		final RecursiveObject recursiveObject1 = new RecursiveObject();
		recursiveObject1.setId(UUID.randomUUID());
		final RecursiveObject recursiveObject2 = new RecursiveObject();
		recursiveObject2.setId(UUID.randomUUID());
		final ReferenceList referenceList = new ReferenceList();
		referenceList.setRecursiveObjects(Arrays.asList(recursiveObject2));
		insert(referenceList);
		final List<ReferenceList> result = select(ReferenceList.class).where(ReferenceList::getRecursiveObjects)
				.is(Arrays.asList(recursiveObject1, recursiveObject2)).execute();

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test(expected = DatabaseException.class)
	public void testConditionNotSet() {
		select(SimpleReference.class).where(SimpleReference::getName).is().execute();
	}

	@Test(expected = MappingException.class)
	public void testCheckCondition() {
		final WhereClause<String, SimpleReference, ?> where = select(SimpleReference.class)
				.where(SimpleReference::getName);
		where.is();
		where.is();
	}

	@Test
	public void testToString() {
		final AbstractStatement<SimpleReference> statement = select(SimpleReference.class)
				.where(SimpleReference::getName).is(NAME_1).and().where(SimpleReference::getValue).is();

		assertEquals(
				"SELECT SimpleReference.* FROM SimpleReference WHERE  SimpleReference.name = 'XX' AND SimpleReference.value = ?",
				statement.toString());
	}

	@Test(expected = DatabaseException.class)
	public void testConnectionLost() throws SQLException{
		getConnection().createStatement().execute("SHUTDOWN");
		final List<SimpleReference> list = select(SimpleReference.class).where(SimpleReference::getName).is().execute(Arrays.asList(NAME_1));
	}
	@Test(expected = DatabaseException.class)
	public void testConnectionLost2() throws SQLException{
		getConnection().createStatement().execute("SHUTDOWN");
		final SimpleReference sr = select(SimpleReference.class).byId(UUID.randomUUID());
	}

	@Test(expected = DatabaseException.class)
	public void testNotSupportedJoin() {
		select(TestClass.class).join(TestClass::getRef);
	}

	private class TestClass {
		private UUID id;
		private NonDefaultConstructor ref;

		public UUID getId() {
			return id;
		}

		public void setId(final UUID id) {
			this.id = id;
		}

		public NonDefaultConstructor getRef() {
			return ref;
		}

		public void setRef(final NonDefaultConstructor ref) {
			this.ref = ref;
		}
	}
}