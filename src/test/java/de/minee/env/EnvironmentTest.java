package de.minee.env;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;

public class EnvironmentTest {

	private Field envCtxField;

	@Before
	public void init() throws Exception {
		envCtxField = Environment.class.getDeclaredField("envCtx");
		envCtxField.setAccessible(true);
		envCtxField.set(null, null);
	}

	@Test
	public void testGetEnvironmentVariable() {
		final String value = Environment.getEnvironmentVariable("key");
		assertEquals("", value);
	}

	@Test
	public void testGet() {
		final Environment environment = new Environment();
		final String value = environment.get("key");
		assertEquals("", value);
	}

	@Test
	public void testGetEnvironmentVariableMock() throws Exception {
		final Hashtable<String, Object> map = new Hashtable<>();
		map.put("key", "value");
		final Context context = new InitialContext() {
			@Override
			public Object lookup(final String name) {
				return map.get(name);
			};
		};
		envCtxField.set(null, context);
		final String value = Environment.getEnvironmentVariable("key");
		assertEquals("value", value);
	}

	@Test
	public void testGetEnvironmentVariableException() throws Exception {
		final Hashtable<String, Object> map = new Hashtable<>();
		map.put("key", "value");
		final Context context = new InitialContext() {
			@Override
			public Object lookup(final String name) throws NamingException {
				throw new NamingException();
			};
		};
		envCtxField.set(null, context);
		final String value = Environment.getEnvironmentVariable("key");
		assertEquals("", value);
	}
}
