package de.minee.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import de.minee.datamodel.SimpleReference;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class SelectStatementTest extends AbstractDAO {

	private static final String NAME_1 = "XX";
	private static final String NAME_2 = "WW";
	private static final String VALUE_1 = "YY";
	private static final String VALUE_2 = "ZZ";

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
		createTable(SimpleReference.class);
		return 1;
	}

	/**
	 * Inserts some entries for the test cases.
	 *
	 * @throws SQLException SQLException in case of an error
	 */
	@Before
	public void init() throws SQLException {
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
	}

	@Test
	public void testSelectAnd() throws SQLException {
		final List<SimpleReference> result = select(SimpleReference.class).where(SimpleReference::getName).is().and()
				.where(SimpleReference::getValue).is().execute(Arrays.asList(NAME_1, VALUE_1));

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	public void testSelectOr() throws SQLException {
		final List<SimpleReference> result = select(SimpleReference.class).where(SimpleReference::getName).is().or()
				.where(SimpleReference::getValue).is().execute(Arrays.asList(NAME_1, VALUE_1));

		assertNotNull(result);
		assertEquals(3, result.size());
	}

	@Test
	public void testQuery() throws SQLException {
		final List<SimpleReference> result = select(SimpleReference.class).query("").execute();
	}

}
