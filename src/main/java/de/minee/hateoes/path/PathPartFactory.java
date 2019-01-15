package de.minee.hateoes.path;

public final class PathPartFactory {

	private PathPartFactory() {
		// No instance needed for a static class
	}

	public static IPathPart create(final Class<?> baseClass, final String pathPart) {
		if (pathPart.startsWith("{") && pathPart.endsWith("}")) {
			final String elPathPart = pathPart.substring(1, pathPart.length() - 1);
			if (elPathPart.contains(".")) {
				return new JoinPathPart(baseClass, elPathPart);
			}
			return new SimplePathPart(elPathPart);
		}
		return new ConstantPathPart(pathPart);
	}

}
