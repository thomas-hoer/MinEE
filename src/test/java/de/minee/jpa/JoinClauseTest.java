package de.minee.jpa;

import static org.junit.Assert.assertEquals;

import de.minee.datamodel.ArrayTypes;
import de.minee.datamodel.EnumObject;
import de.minee.datamodel.PrimitiveList;
import de.minee.datamodel.RecursiveObject;
import de.minee.datamodel.ReferenceChain;
import de.minee.datamodel.ReferenceList;
import de.minee.datamodel.SimpleReference;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

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
	public void testJoin() throws SQLException {
		final UUID id = UUID.randomUUID();
		final SimpleReference simpleReference = new SimpleReference();
		simpleReference.setId(id);
		final ReferenceChain referenceChain = new ReferenceChain();
		referenceChain.setName("xyz");
		referenceChain.setSimpleReference(simpleReference);

		insert(referenceChain);

		final List<SimpleReference> selectedElementsRef = select(SimpleReference.class).execute();
		assertEquals(1, selectedElementsRef.size());

		final List<SimpleReference> selectedElements = select(SimpleReference.class).join(ReferenceChain.class)
				.on(ReferenceChain::getSimpleReference).where(ReferenceChain::getName).is("abc").end()
				.where(SimpleReference::getId).is(id).execute();

		assertEquals(0, selectedElements.size());
	}
}
