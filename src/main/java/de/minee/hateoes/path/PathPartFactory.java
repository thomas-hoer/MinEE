package de.minee.hateoes.path;

import de.minee.hateoes.HateoesServlet.HateoesContext;

public final class PathPartFactory {

	private PathPartFactory() {
		// No instance needed for a static class
	}

	public static <T> IPathPart<T> create(final HateoesContext context, final Class<T> baseClass,
			final String pathPart) {
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
