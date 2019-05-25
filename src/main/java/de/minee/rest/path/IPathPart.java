package de.minee.rest.path;

public interface IPathPart {

	/**
	 * Returns true if the pathPart is a valid parameter for this Resource.
	 *
	 * @param pathPart Path to check
	 * @return true if pathPart is suitable as parameter
	 */
	boolean isMatch(String pathPart);

}
