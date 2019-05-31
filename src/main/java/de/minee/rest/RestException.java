package de.minee.rest;

public class RestException extends RuntimeException {

	private static final long serialVersionUID = -6244037685618551767L;

	public RestException(final String message) {
		super(message);
	}

	public RestException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
