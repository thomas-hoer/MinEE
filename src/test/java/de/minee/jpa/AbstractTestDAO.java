package de.minee.jpa;

public abstract class AbstractTestDAO extends AbstractDAO {

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

}
