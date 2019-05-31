package de.minee.rest.path;

import de.minee.util.Assertions;

public class ParameterPathPart implements IParameterPathPart {

	private final String pathPart;

	public ParameterPathPart(final String pathPart) {
		Assertions.assertNotNull(pathPart, "pathPart should not be null");
		this.pathPart = pathPart;
	}

	@Override
	public boolean isMatch(final String pathPart) {
		return true;
	}

	@Override
	public boolean mathParamName(final String paramPart) {
		return pathPart.equals(paramPart);
	}

	@Override
	public String toString() {
		return String.format("{%s}", pathPart);
	}

}
