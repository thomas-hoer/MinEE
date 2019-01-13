package de.minee.jpa;

import java.sql.SQLException;

public class DAOImpl extends AbstractTestDAO {

	@Override
	protected int updateDatabaseSchema(final int oldDbSchemaVersion) throws SQLException {
		return 0;
	}

}
