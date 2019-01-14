package de.minee.hateoes.path;

import de.minee.util.Assertions;

public class ContantPathPart implements IPathPart {

	private final String path;

	public ContantPathPart(final String path) {
		Assertions.assertNotNull(path);
		this.path = path;
	}

	@Override
	public boolean isMatch(final String path) {
		return this.path.equals(path);
	}

	@Override
	public String toString() {
		return path;
	}

	@Override
	public boolean isParameterType() {
		return false;
	}

	@Override
	public String getFieldName() {
		return null;
	}

}
