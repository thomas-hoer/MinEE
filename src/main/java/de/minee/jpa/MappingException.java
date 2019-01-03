package de.minee.jpa;

public class MappingException extends RuntimeException {

	private static final long serialVersionUID = -5817916767592326322L;

	public MappingException(final String message) {
		super(message);
	}

	public MappingException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public MappingException(final Throwable cause) {
		super(cause);
	}
}
