package de.minee.jpa;

import java.sql.SQLException;

public class DAOImpl extends AbstractDAO {

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
		return 0;
	}

}
