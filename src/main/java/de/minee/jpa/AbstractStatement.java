package de.minee.jpa;

import de.minee.util.Assertions;

import java.sql.Connection;
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
	private final List<AbstractAndOrConnection<T, ? extends AbstractStatement<T>>> connections = new ArrayList<>();
	private final List<AbstractJoinClause<?, T>> joins = new ArrayList<>();
	private String additionalWhereClause;

	/**
	 * Creates a base for a SELECT statement.
	 *
	 * @param cls        Class that corresponds to a database table
	 * @param connection Database connection
	 */
	public AbstractStatement(final Class<T> cls, final Connection connection) {
		super(connection);
		Assertions.assertNotNull(cls, "The type of a table cannot be null. Please pass over a valid Table-Class");
		this.clazz = cls;
	}

	public <U extends AbstractStatement<T>> void add(final AbstractAndOrConnection<T, U> andOrConnection) {
		connections.add(andOrConnection);
	}

	public <S> void add(final AbstractJoinClause<S, T> joinClause) {
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
	 * @return Object with Id id or null if no entry can be found @ SQLException in
	 *         case of an error
	 */
	public T byId(final UUID id) {
		return byId(id, new HashMap<>());
	}

	/**
	 * Selects an Object directly by its Id or returns a cached Object of the
	 * current session. The cache improves performance, ensures object references
	 * and avoids endless cycles.
	 *
	 * @param id             Id of the entry
	 * @param handledObjects Object cache of fetched entries in the same session
	 * @return Object with Id id or null if no entry can be found @ SQLException in
	 *         case of an error
	 */
	protected abstract T byId(final UUID id, final Map<Object, Object> handledObjects);

	/**
	 * Creates a fully assembled and executable Select Query.
	 *
	 * @return Select Query
	 */
	protected String assembleFullSelectQuery() {
		final StringBuilder query = new StringBuilder();
		query.append("SELECT " + clazz.getSimpleName() + ".* FROM ");
		query.append(clazz.getSimpleName());
		query.append(" ");
		for (final AbstractJoinClause<?, T> join : joins) {
			query.append(join.assembleQuery());
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

	/**
	 * Assembles the restriction part of the the query. This can be for example the
	 * part of where excluding the 'WHERE' keyword
	 *
	 * @return The 'WHERE' part of the statement
	 */
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

	/**
	 * Returns the table class for this statement.
	 *
	 * @return Table Class for the Statement, ensured to be not Null
	 */
	public Class<T> getType() {
		return clazz;
	}

	/**
	 * Adds a plain SQL Where string to the Query. Example:
	 *
	 * <pre>
	 * select(Foo.class).query("id = 1").execute()
	 * </pre>
	 *
	 * @param whereClause Plain SQL Where part.
	 * @return this for further processing.
	 */
	public AbstractStatement<T> query(final String whereClause) {
		Assertions.assertNotEmpty(whereClause, "");
		additionalWhereClause = whereClause;
		return this;
	}

	public abstract List<T> execute();

	public abstract List<T> execute(Collection<?> args);

	@Override
	public String toString() {
		return assembleFullSelectQuery();
	}
}
