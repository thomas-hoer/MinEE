package de.minee.hateoes.path;

public class PathPartFactory {

	private PathPartFactory() {
		// No instance needed for a static class
	}

	public static IPathPart create(final String pathPart) {
		if (pathPart.startsWith("{") && pathPart.endsWith("}")) {
			return new SimplePathPart(pathPart.substring(1, pathPart.length() - 1));
		}
		return new ContantPathPart(pathPart);
	}

}
