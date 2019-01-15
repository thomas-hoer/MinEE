package de.minee.jpa;

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

	private final AbstractStatement<T> originalStatement;

	public JoinClause(final AbstractStatement<T> abstractStatement, final Class<S> cls, final Connection connection) {
		super(cls, connection);
		this.originalStatement = abstractStatement;
	}

	public <U> InitialQueryConnection<T> on(final Function<U, S> whereField) throws SQLException {
		return new InitialQueryConnection(this);
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

}
