package de.minee.jpa;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class InitialQueryConnection<T, U extends AbstractStatement<T>> extends AbstractAndOrConnection<T, U> {

	private final Connection connection;

	public InitialQueryConnection(final U statement, final Connection connection) {
		super(statement);
		this.connection = connection;
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

	public <S> JoinClause<S, T> join(final Class<S> cls) {
		final JoinClause<S, T> joinClause = new JoinClause<>(this, cls, connection);
		getStatement().add(joinClause);
		return joinClause;
	}

	@Override
	protected String getConnectionString() {
		return " ";
	}
}
