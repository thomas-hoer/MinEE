package de.minee.hateoes.path;

public interface IPathPart {

	/**
	 * Returns true if the pathPart is a valid parameter for this Resource.
	 *
	 * @param pathPart Path to check
	 * @return true if pathPart is suitable as parameter
	 */
	boolean isMatch(String pathPart);

	/**
	 * If isParameterType() returns true it must implement getFieldName()
	 *
	 * @return true if the Resource depends on this input
	 */
	boolean isParameterType();

	/**
	 * Returns the field name for the corresponding database table.
	 * 
	 * @return
	 */
	String getFieldName();
}
