package de.minee.jpa;

import de.minee.util.ProxyFactory;
import de.minee.util.ProxyFactory.ProxyException;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Function;

/**
 * Joins the class S to T for additional where conditions.
 *
 * @param <S> The class that should be joined
 * @param <T> The base class of the From clause
 */
public class JoinClause<S, T> extends AbstractStatement<S> {

	private final InitialQueryConnection<T, ?> originalStatement;
	private String proxyFieldName;

	public JoinClause(final InitialQueryConnection<T, ?> abstractStatement, final Class<S> cls,
			final Connection connection) {
		super(cls, connection);
		this.originalStatement = abstractStatement;
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

	@Override
	void handleList(final S obj, final Field field, final Map<Object, Object> handledObjects)
			throws SQLException, IllegalAccessException {
		// TODO Auto-generated method stub

	}

	@Override
	void handleFieldColumn(final Field field, final ResultSet rs, final S obj, final Map<Object, Object> handledObjects)
			throws SQLException, IllegalAccessException {
		// TODO Auto-generated method stub

	}

	public InitialQueryConnection<T, ?> end() {
		return originalStatement;
	}

}
