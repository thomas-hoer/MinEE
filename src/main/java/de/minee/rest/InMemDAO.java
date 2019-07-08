package de.minee.rest;

import de.minee.jpa.AbstractDAO;

import java.util.Set;

final class InMemDAO extends AbstractDAO {
	private final Set<Class<?>> inMemTypes;

	InMemDAO(final Set<Class<?>> inMemTypes) {
		this.inMemTypes = inMemTypes;
	}

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
	protected int updateDatabaseSchema(final int oldDbSchemaVersion) {
		for (final Class<?> type : inMemTypes) {
			createTable(type);
		}
		return 1;
	}
}
