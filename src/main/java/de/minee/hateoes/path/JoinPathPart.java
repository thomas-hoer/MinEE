package de.minee.hateoes.path;

public class JoinPathPart implements IPathPart {

	public final String path;

	public JoinPathPart(final String path) {
		this.path = path;
	}

	@Override
	public boolean isMatch(final String path) {
		return true;
	}

	@Override
	public String toString() {
		return path;
	}

	@Override
	public boolean isParameterType() {
		return true;
	}

	@Override
	public String getFieldName() {
		return path;
	}

}
