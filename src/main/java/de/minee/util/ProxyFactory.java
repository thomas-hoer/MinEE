package de.minee.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;

public final class ProxyFactory {

	private ProxyFactory() {
		// Static Class don't need an instance.
	}

	@SuppressWarnings("unchecked")
	public static <T> T getProxy(final Class<T> cls) throws SQLException {
		final javassist.util.proxy.ProxyFactory proxyFactory = new javassist.util.proxy.ProxyFactory();
		proxyFactory.setSuperclass(cls);
		final MethodHandler handler = new ProxyMethodHandler();
		try {
			final Object proxy = proxyFactory.create(new Class<?>[0], new Object[0]);
			((Proxy) proxy).setHandler(handler);
			return (T) proxy;
		} catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException
				| InvocationTargetException e) {
			throw new SQLException("Cannot instantiate Object of type " + cls.getName(), e);
		}
	}

	private static class ProxyMethodHandler implements MethodHandler {

		private static final int SUBSTR_GET = 3;
		private String lastCalledMethod;

		@Override
		public Object invoke(final Object self, final Method thisMethod, final Method proceed, final Object[] args) {
			final String methodName = thisMethod.getName();
			if ("toString".equals(methodName)) {
				return lastCalledMethod;
			}
			lastCalledMethod = methodName.substring(SUBSTR_GET);
			return null;
		}

	}
}
