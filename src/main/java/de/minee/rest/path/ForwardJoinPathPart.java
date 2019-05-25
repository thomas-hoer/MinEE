package de.minee.rest.path;

import de.minee.jpa.AbstractStatement;
import de.minee.jpa.InitialQueryConnection;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Handles restrictions on the resources child elements.
 *
 * <p>
 * For example:
 * 
 * <pre>
 * &#64;HateoesResource("/type/{child.attr}")
 * MyType myType;
 * </pre>
 * 
 * will result in a query like
 * 
 * <pre>
 * SELECT MyType.* FROM MyType Join ChildType on ChildType.id = MyType.child WHERE ChildType.attr = {child.attr}
 * MyType myType;
 * </pre>
 * </p>
 *
 * @param <T> Type of the Resource that is addressed
 */
public class ForwardJoinPathPart<T> extends AbstractVariablePathPart<T> {

	private final Method method;
	private final Method whereMethod;

	public ForwardJoinPathPart(final Class<T> baseClass, final String path) {
		super(path);
		final String[] propertyPath = path.split("\\.");
		final String connectionProperty = propertyPath[0];
		final String whereProperty = propertyPath[1];
		this.method = ReflectionUtil.getMethod(baseClass, "get" + connectionProperty);
		final Class<?> joinClass = method.getReturnType();
		this.whereMethod = ReflectionUtil.getMethod(joinClass, "get" + whereProperty);
	}

	@Override
	public void appendQuery(final InitialQueryConnection<T, AbstractStatement<T>> query) throws SQLException {
		final Function<T, Object> function = t -> ReflectionUtil.invoke(method, t);
		final UnaryOperator<Object> whereField = t -> ReflectionUtil.invoke(whereMethod, t);
		query.join(function).where(whereField).is();

	}
}
