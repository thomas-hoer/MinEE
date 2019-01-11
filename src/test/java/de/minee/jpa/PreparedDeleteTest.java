package de.minee.jpa;

import de.minee.datamodel.ArrayTypes;
import de.minee.datamodel.EnumObject;
import de.minee.datamodel.PrimitiveList;
import de.minee.datamodel.RecursiveObject;
import de.minee.datamodel.ReferenceChain;
import de.minee.datamodel.ReferenceList;
import de.minee.datamodel.SimpleReference;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

public class PreparedDeleteTest extends AbstractDAO {

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
	public void testDelete() throws SQLException {
		final SimpleReference simpleReference = new SimpleReference();

		insert(simpleReference);

		final SimpleReference selectedElement = select(SimpleReference.class).byId(simpleReference.getId());

		delete(selectedElement);

		final SimpleReference deletedElement = select(SimpleReference.class).byId(simpleReference.getId());

		Assert.assertNotNull(selectedElement);
		Assert.assertNull(deletedElement);
	}

}
