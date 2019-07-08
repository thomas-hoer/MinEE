package de.minee.rest.path;

import de.minee.rest.HateoesContext;

public final class PathPartFactory {

	private PathPartFactory() {
		// No instance needed for a static class
	}

	public static IParameterPathPart createSimple(final String pathPart) {
		if (pathPart.startsWith("{") && pathPart.endsWith("}")) {
			final String elPathPart = pathPart.substring(1, pathPart.length() - 1);
			return new ParameterPathPart(elPathPart);
		}
		return new ConstantPathPart(pathPart);
	}

	public static IPathPart create(final HateoesContext context, final Class<?> baseClass, final String pathPart) {
		if (pathPart.startsWith("{") && pathPart.endsWith("}")) {
			final String elPathPart = pathPart.substring(1, pathPart.length() - 1);
			if (elPathPart.contains("\\")) {
				return new BackwardJoinPathPart<>(context, elPathPart);
			} else if (elPathPart.contains(".")) {
				return new ForwardJoinPathPart<>(baseClass, elPathPart);
			}
			return new SimplePathPart<>(baseClass, elPathPart);
		}
		return new ConstantPathPart(pathPart);
	}

}
