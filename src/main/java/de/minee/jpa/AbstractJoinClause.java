package de.minee.jpa;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Joins the class S to T for additional where conditions.
 *
 * @param <S> The class that should be joined
 * @param <T> The base class of the From clause
 */
public abstract class AbstractJoinClause<S, T> extends AbstractStatement<S> {

	private final InitialQueryConnection<T, ? extends AbstractStatement<T>> queryConnection;

	AbstractJoinClause(final InitialQueryConnection<T, ? extends AbstractStatement<T>> queryConnection,
			final Class<S> cls, final Connection connection) {
		super(cls, connection);
		this.queryConnection = queryConnection;
	}

	@SuppressWarnings("unchecked")
	protected <U extends AbstractStatement<T>> InitialQueryConnection<T, U> getQueryConnection() {
		return (InitialQueryConnection<T, U>) queryConnection;
	}

	@SuppressWarnings("unchecked")
	public <U extends AbstractStatement<T>> InitialQueryConnection<T, U> end() {
		return (InitialQueryConnection<T, U>) queryConnection;
	}

	@Override
	protected String assembleQuery() {
		final StringBuilder stringBuilder = new StringBuilder();
		final String additionalConditions = super.assembleQuery();
		if (!"".equals(additionalConditions)) {
			stringBuilder.append("AND ").append(additionalConditions).append(" ");
		}
		return stringBuilder.toString();
	}

	@Override
	public List<S> execute() {
		throw new IllegalStateException(
				"Not able to execute a unfinished Join clause. Please add .end() before .execute() in order to complete the Statement");
	}

	@Override
	public List<S> execute(final Collection<?> args) {
		throw new IllegalStateException(
				"Not able to execute a unfinished Join clause. Please add .end() before .execute() in order to complete the Statement");
	}

	@Override
	protected S byId(final UUID id, final Map<Object, Object> handledObjects) {
		throw new IllegalStateException(
				"Not able to execute a unfinished Join clause. Please add .end() before .byId() in order to complete the Statement");
	}
}
