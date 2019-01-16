package de.minee.jpa;

import de.minee.util.ProxyFactory;
import de.minee.util.ProxyFactory.ProxyException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Joins the class S to T for additional where conditions.
 *
 * @param <S> The class that should be joined
 * @param <T> The base class of the From clause
 */
public class JoinClause<S, T> extends AbstractStatement<S> {

	private final InitialQueryConnection<T, ?> queryConnection;
	private String proxyFieldName;

	public JoinClause(final InitialQueryConnection<T, ?> queryConnectio, final Class<S> cls,
			final Connection connection) {
		super(cls, connection);
		this.queryConnection = queryConnectio;
	}

	public InitialQueryConnection<S, JoinClause<S, T>> on(final Function<S, T> whereField) throws SQLException {
		S proxy;
		try {
			proxy = ProxyFactory.getProxy(getType());
		} catch (final ProxyException e) {
			throw new SQLException(e);
		}
		whereField.apply(proxy);
		proxyFieldName = proxy.toString();
		return new InitialQueryConnection<>(this, getConnection());
	}

	@SuppressWarnings("unchecked")
	public <U extends AbstractStatement<T>> InitialQueryConnection<T, U> end() {
		return (InitialQueryConnection<T, U>) queryConnection;
	}

	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("JOIN ");
		stringBuilder.append(getType().getSimpleName());
		stringBuilder.append(" ON ");
		stringBuilder.append(getType().getSimpleName());
		stringBuilder.append(".");
		stringBuilder.append(proxyFieldName);
		stringBuilder.append(" = ");
		stringBuilder.append(queryConnection.getStatement().getType().getSimpleName());
		stringBuilder.append(".id ");

		final String additionalConditions = assembleQuery();
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
