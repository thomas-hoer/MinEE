package de.minee.rest.path;

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
public final class ConstantPathPart implements IParameterPathPart {

	private final String pathConstant;

	public ConstantPathPart(final String path) {
		Assertions.assertNotNull(path, "Path should not be null");
		this.pathConstant = path;
	}

	/**
	 * The path part is only a match if the path is exactly the same.
	 */
	@Override
	public boolean isMatch(final String path) {
		return this.pathConstant.equals(path);
	}

	@Override
	public String toString() {
		return pathConstant;
	}

	@Override
	public boolean mathParamName(final String paramPart) {
		return false;
	}

}
