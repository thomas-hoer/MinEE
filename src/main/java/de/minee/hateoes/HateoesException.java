package de.minee.hateoes;

public class HateoesException extends RuntimeException {

	private static final long serialVersionUID = -6244037685618551767L;

	public HateoesException(final String message) {
		super(message);
	}

	public HateoesException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public HateoesException(final Throwable cause) {
		super(cause);
	}

}
