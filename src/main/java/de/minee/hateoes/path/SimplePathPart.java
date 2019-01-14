package de.minee.hateoes.path;

public class SimplePathPart implements IPathPart {

	private final String path;

	public SimplePathPart(final String path) {
		this.path = path;
	}

	@Override
	public boolean isMatch(final String path) {
		return true;
	}

	@Override
	public boolean isParameterType() {
		return true;
	}

	@Override
	public String getFieldName() {
		return path;
	}

	@Override
	public String toString() {
		return String.format("{%s}", path);
	}

}
