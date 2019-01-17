package de.minee.hateoes.path;

public class SimplePathPart extends AbstractVariablePathPart {

	public SimplePathPart(final String path) {
		super(path);
	}

	@Override
	public String getFieldName() {
		return getPath();
	}

}
