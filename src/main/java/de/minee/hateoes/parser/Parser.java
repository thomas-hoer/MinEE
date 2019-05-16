package de.minee.hateoes.parser;

public interface Parser {

	<T> T parse(String payload, Class<T> type) throws ParserException;
}
