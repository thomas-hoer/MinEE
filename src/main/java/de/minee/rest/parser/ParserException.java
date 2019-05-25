package de.minee.rest.parser;

/**
 *
 */
public class ParserException extends Exception {

	private static final long serialVersionUID = 5678460520945773458L;

	public ParserException(final String msg) {
		super(msg);
	}

	public ParserException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	public ParserException(final Throwable cause) {
		super(cause);
	}
}
