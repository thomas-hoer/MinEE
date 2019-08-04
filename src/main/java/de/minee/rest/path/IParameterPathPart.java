package de.minee.rest.path;

public interface IParameterPathPart extends IPathPart {

	boolean matchParamName(String paramPart);
}
