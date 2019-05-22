package de.minee.hateoes.parser;

public interface Parser {

	<T> T parse(String payload, Class<T> type) throws ParserException;

	/**
	 * This method is for checking if the parser can parse the given content type.
	 * It is possible that a parser can parse different contentTypes.
	 *
	 * @param contentType MIME Type of the content
	 * @return true if the parser can handle the given content type
	 */
	boolean accept(String contentType);
}
