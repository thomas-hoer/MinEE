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
import java.util.Arrays;
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
	public void testJoinNotMatching() throws SQLException {
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

	@Test
	public void testJoinMatching() throws SQLException {
		final UUID id = UUID.randomUUID();
		final SimpleReference simpleReference1 = new SimpleReference();
		simpleReference1.setName("sr1");
		final ReferenceChain referenceChain1 = new ReferenceChain();
		referenceChain1.setName("xyz");
		referenceChain1.setSimpleReference(simpleReference1);

		final SimpleReference simpleReference2 = new SimpleReference();
		simpleReference2.setName("sr1");
		final ReferenceChain referenceChain2 = new ReferenceChain();
		referenceChain2.setName("abc");
		referenceChain2.setSimpleReference(simpleReference2);

		insert(referenceChain1);
		insert(referenceChain2);

		final List<SimpleReference> selectedElementsRef = select(SimpleReference.class).execute();
		assertEquals(2, selectedElementsRef.size());

		final List<SimpleReference> selectedElements = select(SimpleReference.class).join(ReferenceChain.class)
				.on(ReferenceChain::getSimpleReference).where(ReferenceChain::getName).is("abc").end()
				.where(SimpleReference::getName).is("sr1").execute();

		assertEquals(1, selectedElements.size());
	}

	@Test
	public void testForwardJoinMatching() throws SQLException {
		final SimpleReference simpleReference1 = new SimpleReference();
		simpleReference1.setName("sr");

		final SimpleReference simpleReference2 = new SimpleReference();
		simpleReference2.setName("sr2");
		final ReferenceChain referenceChain2 = new ReferenceChain();
		referenceChain2.setName("abc");
		simpleReference2.setReferenceChain(referenceChain2);

		final SimpleReference simpleReference3 = new SimpleReference();
		simpleReference3.setName("sr");
		final ReferenceChain referenceChain3 = new ReferenceChain();
		referenceChain3.setName("abcd");
		simpleReference3.setReferenceChain(referenceChain3);

		insert(simpleReference1);
		insert(simpleReference2);
		insert(simpleReference3);

		final List<SimpleReference> selectedElements1 = select(SimpleReference.class)
				.join(SimpleReference::getReferenceChain).where(ReferenceChain::getName).is("abc").end().execute();
		assertEquals(1, selectedElements1.size());

	}

	@Test(expected = IllegalStateException.class)
	public void testUnfinishedQuery() throws SQLException {
		select(SimpleReference.class).join(ReferenceChain.class).execute();
	}

	@Test(expected = IllegalStateException.class)
	public void testUnfinishedQueryParams() throws SQLException {
		select(SimpleReference.class).join(ReferenceChain.class).execute(Arrays.asList());
	}

	@Test(expected = IllegalStateException.class)
	public void testUnfinishedQueryById() throws SQLException {
		final UUID id = UUID.randomUUID();
		select(SimpleReference.class).join(ReferenceChain.class).byId(id);
	}
}
