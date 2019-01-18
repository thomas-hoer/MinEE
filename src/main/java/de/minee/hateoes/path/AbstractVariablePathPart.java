package de.minee.hateoes.path;

public abstract class AbstractVariablePathPart<T> implements IPathPart<T> {

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

	@Override
	public String toString() {
		return String.format("{%s}", path);
	}

}
