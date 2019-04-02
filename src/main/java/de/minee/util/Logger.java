package de.minee.util;

import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Central class for MinEE Logging purposes. This makes it easy to may switch
 * from java.util.logging.Logger to Log4J someday.
 *
 */
public final class Logger {

	private final java.util.logging.Logger javaUtilLogger;

	private Logger(final Class<?> cls) {
		javaUtilLogger = java.util.logging.Logger.getLogger(cls.getName());
	}

	public static Logger getLogger(final Class<?> cls) {
		return new Logger(cls);
	}

	public void info(final String message) {
		javaUtilLogger.log(Level.INFO, message);
	}

	public void info(final Supplier<String> msgSupplier) {
		javaUtilLogger.info(msgSupplier);
	}

	public void warn(final String message) {
		javaUtilLogger.log(Level.WARNING, message);
	}

	public void warn(final String message, final Throwable throwable) {
		javaUtilLogger.log(Level.WARNING, message, throwable);
	}

	public void error(final String message, final Throwable throwable) {
		javaUtilLogger.log(Level.SEVERE, message, throwable);
	}
}
