package de.minee.env;

import de.minee.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * The aim of this class is to get easy access to environment based properties.
 * On a Tomcat server you can define the values in the context.xml like this:
 *
 * <pre>
 * {@code
 * <Context>
 *    <Environment name="NAME" type="java.lang.String" value="VALUE"/>
 * </Context>
 * }
 * </pre>
 */
public class Environment {

	private static final Logger LOGGER = Logger.getLogger(Environment.class);

	private static Context initCtx;
	private static Context envCtx;

	static {
		try {
			initCtx = new InitialContext();
			envCtx = (Context) initCtx.lookup("java:comp/env");
		} catch (final NamingException e) {
			LOGGER.error("Cannot load Environment Variables", e);
		}
	}

	/**
	 * Get the value of an environment variable.
	 *
	 * @param key Key of the environment variable
	 * @return Value of the environment variable
	 */
	public static String getEnvironmentVariable(final String key) {
		if (envCtx == null) {
			return "";
		}
		try {
			return (String) envCtx.lookup(key);
		} catch (final NamingException e) {
			LOGGER.warn("Failed to lookup key " + key, e);
			return "";
		}
	}

	/**
	 * Get the value of an environment variable.
	 *
	 * @param key Key of the environment variable
	 * @return Value of the environment variable
	 */
	public String get(final String key) {
		return getEnvironmentVariable(key);
	}
}
