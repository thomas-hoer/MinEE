package de.minee.jpa;

import de.minee.util.Assertions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Base class for the query builder that contain WHERE restrictions.
 *
 * @param <T> Class that corresponds to a database table
 */
public abstract class AbstractStatement<T> extends AbstractQuery {

	private final Class<T> clazz;
	private final Connection connection;
	private final List<AbstractAndOrConnection<T, ? extends AbstractStatement<T>>> connections = new ArrayList<>();
	private final List<JoinClause<?, T>> joins = new ArrayList<>();
	private String additionalWhereClause;

	/**
	 * Creates a base for a SELECT statement.
	 *
	 * @param cls        Class that corresponds to a database table
	 * @param connection Database connection
	 */
	public AbstractStatement(final Class<T> cls, final Connection connection) {
		Assertions.assertNotNull(cls);
		Assertions.assertNotNull(connection);
		this.clazz = cls;
		this.connection = connection;
	}

	public <U extends AbstractStatement<T>> void add(final AbstractAndOrConnection<T, U> connection) {
		connections.add(connection);
	}

	public <S> void add(final JoinClause<S, T> joinClause) {
		joins.add(joinClause);
	}

	public AbstractAndOrConnection<T, AbstractStatement<T>> and() {
		return new AndQueryConnection<>(this);
	}

	public AbstractAndOrConnection<T, AbstractStatement<T>> or() {
		return new OrQueryConnection<>(this);
	}

	/**
	 * Selects an Object directly by its Id.
	 *
	 * @param id Id of the entry
	 * @return Object with Id id or null if no entry can be found
	 * @throws SQLException SQLException in case of an error
	 */
	public T byId(final UUID id) throws SQLException {
		return byId(id, new HashMap<>());
	}

	/**
	 * Selects an Object directly by its Id or returns a cached Object of the
	 * current session. The cache improves performance, ensures object references
	 * and avoids endless cycles.
	 *
	 * @param id             Id of the entry
	 * @param handledObjects Object cache of fetched entries in the same session
	 * @return Object with Id id or null if no entry can be found
	 * @throws SQLException SQLException in case of an error
	 */
	protected abstract T byId(final UUID id, final Map<Object, Object> handledObjects) throws SQLException;

	protected String assembleFullSelectQuery() {
		final StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ");
		query.append(clazz.getSimpleName());
		query.append(" ");
		for (final JoinClause<?, T> join : joins) {
			query.append(join);
		}
		for (final AbstractAndOrConnection<T, ?> queryConnection : connections) {
			final WhereClause<?, ?, ?> whereClause = queryConnection.getClause();
			query.append(whereClause.getJoinClause());
		}
		if (!connections.isEmpty() || (additionalWhereClause != null)) {
			query.append("WHERE ");
		}
		query.append(assembleQuery());
		return query.toString();
	}

	protected String assembleQuery() {
		final StringBuilder query = new StringBuilder();
		if (additionalWhereClause != null) {
			query.append(additionalWhereClause);
		}
		for (final AbstractAndOrConnection<T, ?> queryConnection : connections) {
			final WhereClause<?, ?, ?> whereClause = queryConnection.getClause();
			query.append(queryConnection.getConnectionString());
			query.append(whereClause.toString());
		}
		return query.toString();
	}

	@Override
	protected Connection getConnection() {
		return connection;
	}

	public Class<T> getType() {
		return clazz;
	}

	public AbstractStatement<T> query(final String whereClause) {
		Assertions.assertNotEmpty(whereClause);
		additionalWhereClause = whereClause;
		return this;
	}

	public abstract List<T> execute() throws SQLException;

	public abstract List<T> execute(Collection<?> args) throws SQLException;

}
