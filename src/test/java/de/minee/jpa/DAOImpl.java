package de.minee.jpa;

import java.sql.SQLException;

import de.minee.jpa.AbstractDAO;

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
	protected int updateDatabaseSchema(int oldDbSchemaVersion) throws SQLException {
		return 0;
	}

}
