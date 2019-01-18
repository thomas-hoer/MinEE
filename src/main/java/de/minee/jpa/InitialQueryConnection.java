package de.minee.jpa;

import de.minee.util.ProxyFactory;
import de.minee.util.ProxyFactory.ProxyException;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

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

	public List<T> execute(final Collection<?> args) throws SQLException {
		return getStatement().execute(args);
	}

	public AbstractStatement<T> query(final String manualQuery) {
		return getStatement().query(manualQuery);
	}

	public <S> BackwardJoinClause<S, T> join(final Class<S> cls) {
		final BackwardJoinClause<S, T> joinClause = new BackwardJoinClause<>(this, cls, connection);
		getStatement().add(joinClause);
		return joinClause;
	}

	public <S> InitialQueryConnection<S, AbstractJoinClause<S, T>> join(final Function<T, S> whereField)
			throws SQLException {

		final Class<T> cls = getStatement().getType();
		T proxy;
		try {
			proxy = ProxyFactory.getProxy(cls);
		} catch (final ProxyException e) {
			throw new SQLException(e);
		}
		whereField.apply(proxy);
		final String proxyFieldName = proxy.toString();
		final Method getter = ReflectionUtil.getMethod(cls, "get" + proxyFieldName);
		final Class<S> destClass = (Class<S>) getter.getReturnType();
		final AbstractJoinClause<S, T> joinClause = new ForwardJoinClause<>(this, destClass, proxyFieldName,
				connection);
		final InitialQueryConnection<S, AbstractJoinClause<S, T>> initialQueryConnection = new InitialQueryConnection<>(
				joinClause, connection);
		getStatement().add(joinClause);
		return initialQueryConnection;
	}

	@Override
	protected String getConnectionString() {
		return " ";
	}
}
