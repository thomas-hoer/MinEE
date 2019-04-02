package de.minee.hateoes.path;

import de.minee.hateoes.HateoesServlet.HateoesContext;
import de.minee.jpa.AbstractStatement;
import de.minee.jpa.InitialQueryConnection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Path<T> {

	private final List<IPathPart> pathParts = new ArrayList<>();

	public Path(final HateoesContext context, final String fullPath, final Class<T> type) {
		final String[] paths = fullPath.split("/");
		for (final String pathPart : paths) {
			pathParts.add(PathPartFactory.create(context, type, pathPart));
		}
	}

	public int size() {
		return pathParts.size();
	}

	public boolean isMatch(final String pathInfo) {
		final String[] pathSplit = pathInfo.split("/");
		boolean isMatch = false;
		final int pathSize = pathParts.size();
		if (pathSplit.length == pathSize || pathSplit.length == pathSize + 1) {
			for (int i = 0; i < pathSize; i++) {
				if (!pathParts.get(i).isMatch(pathSplit[i])) {
					return false;
				}
			}
			if (pathSplit.length == pathSize + 1) {
				final String action = pathSplit[pathSize];
				isMatch = "create".equals(action) || "edit".equals(action);
			} else {
				isMatch = true;
			}
		}
		return isMatch;
	}

	public List<T> executeSelect(final String[] pathSplit, final InitialQueryConnection<T, AbstractStatement<T>> select)
			throws SQLException {
		final List<String> parameterForJoin = new ArrayList<>();
		final List<String> parameterForWhere = new ArrayList<>();
		for (int i = 0; i < pathParts.size(); i++) {
			final IPathPart pathPart = pathParts.get(i);
			if (pathPart instanceof AbstractVariablePathPart) {
				((AbstractVariablePathPart<T>) pathPart).appendQuery(select);
				if (pathPart instanceof SimplePathPart) {
					parameterForWhere.add(pathSplit[i]);
				} else {
					parameterForJoin.add(pathSplit[i]);
				}
			}
		}
		parameterForJoin.addAll(parameterForWhere);
		if (parameterForJoin.isEmpty()) {
			return select.execute();
		}
		return select.execute(parameterForJoin);
	}

	@Override
	public String toString() {
		final StringJoiner joiner = new StringJoiner("/");
		pathParts.stream().map(IPathPart::toString).forEach(joiner::add);
		return joiner.toString();
	}
}
