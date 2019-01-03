package de.minee.cdi;

public class CdiException extends RuntimeException {

	private static final long serialVersionUID = -2142610513432856838L;

	public CdiException(final String message) {
		super(message);
	}

	public CdiException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
