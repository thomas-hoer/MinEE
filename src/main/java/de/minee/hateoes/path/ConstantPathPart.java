package de.minee.hateoes.path;

import de.minee.jpa.AbstractStatement;
import de.minee.jpa.InitialQueryConnection;
import de.minee.util.Assertions;

/**
 * Represents the constant parts of a Rest resource path.
 *
 * <p>
 * The 'type' component of the following example will be handled by
 * ConstantPathPart.
 *
 * <pre>
 * &#64;HateoesResource("/type/{id}")
 * MyType myType;
 * </pre>
 * </p>
 *
 */
public final class ConstantPathPart<T> implements IPathPart<T> {

	private final String path;

	public ConstantPathPart(final String path) {
		Assertions.assertNotNull(path);
		this.path = path;
	}

	/**
	 * The path part is only a match if the path is exactly the same.
	 */
	@Override
	public boolean isMatch(final String path) {
		return this.path.equals(path);
	}

	/**
	 * Only for addressing the resource, but not a parameter.
	 */
	@Override
	public boolean isParameterType() {
		return false;
	}

	@Override
	public String toString() {
		return path;
	}

	@Override
	public void appendQuery(final InitialQueryConnection<T, AbstractStatement<T>> query) {
		// Do nothing
	}
}
