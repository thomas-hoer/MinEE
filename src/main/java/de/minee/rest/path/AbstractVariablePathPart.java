package de.minee.rest.path;

import de.minee.jpa.AbstractStatement;
import de.minee.jpa.InitialQueryConnection;

import java.sql.SQLException;

public abstract class AbstractVariablePathPart<T> implements IPathPart {

	private final String variablePath;

	public AbstractVariablePathPart(final String path) {
		this.variablePath = path;
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
		return String.format("{%s}", variablePath);
	}

}
