package de.minee.env;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Environment {

	private static final Logger logger = Logger.getLogger(Environment.class.getName());

	private static Context initCtx;
	private static Context envCtx;
	static {
		try {
			initCtx = new InitialContext();
			envCtx = (Context) initCtx.lookup("java:comp/env");
		} catch (final NamingException e) {
			logger.log(Level.SEVERE, "Cannot load Environment Variables", e);
		}
	}

	public static String getEnvironmentVariable(final String key) {
		try {
			return (String) envCtx.lookup(key);
		} catch (final NamingException e) {
			return "";
		}
	}

	public String get(final String key) {
		return getEnvironmentVariable(key);
	}
}
