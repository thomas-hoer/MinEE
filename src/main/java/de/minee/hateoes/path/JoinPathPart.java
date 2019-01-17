package de.minee.hateoes.path;

public class JoinPathPart extends AbstractVariablePathPart {

	@SuppressWarnings("unused")
	private final Class<?> baseClass;// will be used for join

	public JoinPathPart(final Class<?> baseClass, final String path) {
		super(path);
		this.baseClass = baseClass;
	}

	@Override
	public String getFieldName() {
		return getPath();
	}
}
