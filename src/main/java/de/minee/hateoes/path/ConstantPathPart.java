package de.minee.hateoes.path;

import de.minee.util.Assertions;

public class ConstantPathPart implements IPathPart {

	private final String path;

	public ConstantPathPart(final String path) {
		Assertions.assertNotNull(path);
		this.path = path;
	}

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

	/**
	 * The result of isParameterType is false so there is no need to implement this.
	 */
	@Override
	public String getFieldName() {
		return null;
	}

	@Override
	public String toString() {
		return path;
	}
}
