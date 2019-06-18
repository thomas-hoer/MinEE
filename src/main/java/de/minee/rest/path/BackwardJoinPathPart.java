package de.minee.rest.path;

import de.minee.jpa.AbstractStatement;
import de.minee.jpa.InitialQueryConnection;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class BackwardJoinPathPart<T> extends AbstractVariablePathPart<T> {

	private final Class<Object> joinClass;
	private final Method joinMethod;
	private final Method whereMethod;

	@SuppressWarnings("unchecked")
	public BackwardJoinPathPart(final de.minee.rest.RestServlet.HateoesContext context, final String path) {
		super(path);
		final String[] backwardReference = path.split("\\\\");
		final String backwardClass = backwardReference[0];
		final String[] propertyPath = backwardClass.split("\\.");
		joinClass = (Class<Object>) context.getTypeByName(propertyPath[0]);
		final String joinConnection = backwardReference[1];
		joinMethod = ReflectionUtil.getMethod(joinClass, "get" + joinConnection);
		whereMethod = ReflectionUtil.getMethod(joinClass, "get" + propertyPath[1]);

	}

	@Override
	public void appendQuery(final InitialQueryConnection<T, AbstractStatement<T>> query) {
		@SuppressWarnings("unchecked")
		final Function<Object, T> function = t -> (T) ReflectionUtil.invoke(joinMethod, t);
		final UnaryOperator<Object> whereField = t -> ReflectionUtil.invoke(whereMethod, t);
		query.join(joinClass).on(function).where(whereField).is();
	}
}
