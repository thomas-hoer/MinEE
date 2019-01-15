package de.minee.jpa;

import java.sql.SQLException;
import java.util.function.Function;

/**
 * Base Class for connecting different Where clauses. The standard configuration
 * is, that AND binds stronger than OR. In particular this means that A OR B AND
 * C evaluates A OR (B AND C).
 */
public abstract class AbstractAndOrConnection<T, U extends IStatement<T>> {

	private final U statement;
	private WhereClause<?, T, U> whereClause;

	public AbstractAndOrConnection(final U statement) {
		this.statement = statement;
	}

	/**
	 * Adds a further 'where' clause into the statement.
	 *
	 * @param whereField Getter to the Field that is selected
	 * @return WhereClause to set the relation to the field
	 * @throws SQLException SQLException in case of an error
	 */
	public <S> WhereClause<S, T, U> where(final Function<T, S> whereField) throws SQLException {
		final WhereClause<S, T, U> where = new WhereClause<>(whereField, statement);
		statement.add(this);
		whereClause = where;
		return where;
	}

	<S> WhereClause<S, T, U> getClause() {
		return (WhereClause<S, T, U>) whereClause;
	}

	protected U getStatement() {
		return statement;
	}

	protected abstract String getConnectionString();
}
