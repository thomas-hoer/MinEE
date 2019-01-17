package de.minee.hateoes.path;

public abstract class AbstractVariablePathPart implements IPathPart {

	private final String path;

	public AbstractVariablePathPart(final String path) {
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

	protected String getPath() {
		return path;
	}

	@Override
	public String toString() {
		return String.format("{%s}", path);
	}

}
