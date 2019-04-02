package de.minee.hateoes.path;

import de.minee.jpa.AbstractStatement;
import de.minee.jpa.InitialQueryConnection;

import java.sql.SQLException;

public abstract class AbstractVariablePathPart<T> implements IPathPart {

	private final String path;

	public AbstractVariablePathPart(final String path) {
		this.path = path;
	}

	@Override
	public boolean isMatch(final String path) {
		return true;
	}

	/**
	 * Extends the query.
	 */
	abstract void appendQuery(InitialQueryConnection<T, AbstractStatement<T>> query) throws SQLException;

	@Override
	public String toString() {
		return String.format("{%s}", path);
	}

}
