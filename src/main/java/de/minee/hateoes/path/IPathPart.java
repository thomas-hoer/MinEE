package de.minee.hateoes.path;

import de.minee.jpa.AbstractStatement;
import de.minee.jpa.InitialQueryConnection;

import java.sql.SQLException;

public interface IPathPart<T> {

	/**
	 * Returns true if the pathPart is a valid parameter for this Resource.
	 *
	 * @param pathPart Path to check
	 * @return true if pathPart is suitable as parameter
	 */
	boolean isMatch(String pathPart);

	/**
	 * If isParameterType() returns true it must implement appendQuery().
	 *
	 * @return true if the Resource depends on this input
	 */
	boolean isParameterType();

	/**
	 * Extends the query.
	 */
	void appendQuery(InitialQueryConnection<T, AbstractStatement<T>> query) throws SQLException;
}
