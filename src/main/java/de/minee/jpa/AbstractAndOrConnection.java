package de.minee.jpa;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

/**
 * Base Class for connecting different Where clauses. The standard configuration
 * is, that AND binds stronger than OR. In particular this means that A OR B AND
 * C evaluates A OR (B AND C).
 */
public abstract class AbstractAndOrConnection<T> {

	private final AbstractStatement<T> statement;
	private final Class<T> clazz;
	private final Connection connection;

	public AbstractAndOrConnection(final AbstractStatement<T> statement, final Class<T> clazz,
			final Connection connection) {
		this.statement = statement;
		this.clazz = clazz;
		this.connection = connection;
	}

	public <S> WhereClause<S, T> where(final Function<T, S> whereField) throws SQLException {
		final WhereClause<S, T> where = new WhereClause<S, T>(whereField, statement);
		statement.add(this);
		return where;
	}

}
