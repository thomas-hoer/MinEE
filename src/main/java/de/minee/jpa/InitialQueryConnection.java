package de.minee.jpa;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class InitialQueryConnection<T> extends AbstractAndOrConnection<T> {

	public InitialQueryConnection(final AbstractStatement<T> statement) {
		super(statement);
	}

	public T byId(final UUID id) throws SQLException {
		return getStatement().byId(id);
	}

	public List<T> execute() throws SQLException {
		return getStatement().execute();
	}

	public AbstractStatement<T> query(final String manualQuery) {
		return getStatement().query(manualQuery);
	}

	@Override
	protected String getConnectionString() {
		return " ";
	}
}
