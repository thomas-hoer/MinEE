package de.minee.rest.path;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParameterizedPath {

	private final List<IParameterPathPart> pathParts;

	public ParameterizedPath(final String fullPath) {
		pathParts = Arrays.stream(fullPath.split("/")).map(PathPartFactory::createSimple).collect(Collectors.toList());
	}

	public boolean isMatch(final String pathInfo) {
		final String[] pathSplit = pathInfo.split("/");
		final int pathSize = pathParts.size();
		if (pathSplit.length == pathSize) {
			for (int i = 0; i < pathSize; i++) {
				if (!pathParts.get(i).isMatch(pathSplit[i])) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return pathParts.stream().map(IPathPart::toString).collect(Collectors.joining("/"));
	}

	public int paramPos(final String pathParam) {
		for (int pos = 0; pos < pathParts.size(); pos++) {
			if (pathParts.get(pos).matchParamName(pathParam)) {
				return pos;
			}
		}
		return -1;
	}
}
