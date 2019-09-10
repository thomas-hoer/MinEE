package de.minee.rest.parser;

public class ParserException extends RuntimeException {

	private static final long serialVersionUID = 1L;

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
