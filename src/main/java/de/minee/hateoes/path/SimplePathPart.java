package de.minee.hateoes.path;

import de.minee.jpa.AbstractStatement;
import de.minee.jpa.InitialQueryConnection;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.sql.SQLException;

public class SimplePathPart<T> extends AbstractVariablePathPart<T> {

	private final Method method;

	public SimplePathPart(final Class<T> baseClass, final String path) {
		super(path);
		method = ReflectionUtil.getMethod(baseClass, "get" + path);
	}

	@Override
	public void appendQuery(final InitialQueryConnection<T, AbstractStatement<T>> query) throws SQLException {
		query.where(t -> ReflectionUtil.invoke(method, t)).is();
	}

}
