package de.minee.jpa;

import de.minee.datamodel.ArrayTypes;
import de.minee.datamodel.EnumObject;
import de.minee.datamodel.PrimitiveList;
import de.minee.datamodel.RecursiveObject;
import de.minee.datamodel.ReferenceChain;
import de.minee.datamodel.ReferenceList;
import de.minee.datamodel.SimpleReference;

import java.sql.SQLException;

import org.junit.Test;

public class JoinClauseTest extends AbstractTestDAO {

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
	public void test() throws SQLException {
		/*
		 * final UUID id = UUID.randomUUID();
		 * select(PrimitiveList.class).join(RecursiveObject.class).on(RecursiveObject::
		 * getChild)
		 * .where(RecursiveObject::getName).is("abc").end().where(PrimitiveList::getId).
		 * is(id);
		 */
	}
}
