package de.minee.hateoes.path;

public class JoinPathPart implements IPathPart {

	private final String path;
	private final Class<?> baseClass;

	public JoinPathPart(final Class<?> baseClass, final String path) {
		final String[] backwardReference = path.split("\\\\");
		final String lastElement = backwardReference[backwardReference.length - 1];
		this.path = lastElement;
		this.baseClass = baseClass;
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
		return path;
	}
}
