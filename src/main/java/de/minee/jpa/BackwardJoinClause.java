package de.minee.jpa;

import de.minee.util.ProxyFactory;
import de.minee.util.ProxyFactory.ProxyException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public class BackwardJoinClause<S, T> extends AbstractJoinClause<S, T> {

	private String proxyFieldName;

	BackwardJoinClause(final InitialQueryConnection<T, ?> queryConnectio, final Class<S> cls,
			final Connection connection) {
		super(queryConnectio, cls, connection);
	}

	public InitialQueryConnection<S, AbstractJoinClause<S, T>> on(final Function<S, T> whereField) throws SQLException {
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

	@Override
	protected String assembleQuery() {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("JOIN ");
		stringBuilder.append(getType().getSimpleName());
		stringBuilder.append(" ON ");
		stringBuilder.append(getType().getSimpleName());
		stringBuilder.append(".");
		stringBuilder.append(proxyFieldName);
		stringBuilder.append(" = ");
		stringBuilder.append(getQueryConnection().getStatement().getType().getSimpleName());
		stringBuilder.append(".id ");

		stringBuilder.append(super.assembleQuery());
		return stringBuilder.toString();
	}
}
