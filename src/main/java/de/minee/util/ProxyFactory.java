package de.minee.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;

public final class ProxyFactory {

	private ProxyFactory() {
		// Static Class don't need an instance.
	}

	/**
	 * Creates a proxy instance on which you can check what method is called. The
	 * last called method can be retrieved by invoking toString().
	 *
	 * @param cls                Type of the proxy
	 * @param objects
	 * @param classes
	 * @param proxyMethodHandler
	 * @return proxy instance of type cls
	 * @throws ProxyException ProxyException in case of an Error
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getProxy(final Class<T> cls) throws ProxyException {
		return getProxy(cls, new ProxyMethodHandler(), new Class<?>[0], new Object[0]);
	}

	/**
	 * Creates a proxy instance which is intercepted by the methodHandler.
	 *
	 * @param cls           Type of the proxy
	 * @param methodHandler
	 * @return proxy instance of type cls
	 * @throws ProxyException ProxyException in case of an Error
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getProxy(final Class<T> cls, final MethodHandler methodHandler, final Class<?>[] paramTypes,
			final Object[] args) throws ProxyException {
		final javassist.util.proxy.ProxyFactory proxyFactory = new javassist.util.proxy.ProxyFactory();
		proxyFactory.setSuperclass(cls);
		try {
			final Object proxy = proxyFactory.create(paramTypes, args);
			((Proxy) proxy).setHandler(methodHandler);
			return (T) proxy;
		} catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException
				| InvocationTargetException e) {
			throw new ProxyException("Cannot instantiate Object of type " + cls.getName(), e);
		}
	}

	private static class ProxyMethodHandler implements MethodHandler {

		private static final int SUBSTR_IS = 2;
		private static final int SUBSTR_GET = 3;
		private String lastCalledProperty;

		@Override
		public Object invoke(final Object self, final Method thisMethod, final Method proceed, final Object[] args) {
			final String methodName = thisMethod.getName();
			if ("toString".equals(methodName)) {
				return lastCalledProperty;
			}
			if (methodName.startsWith("is")) {
				lastCalledProperty = methodName.substring(SUBSTR_IS);
			} else if (methodName.startsWith("get")) {
				lastCalledProperty = methodName.substring(SUBSTR_GET);
			}
			lastCalledProperty = lastCalledProperty.substring(0, 1).toLowerCase() + lastCalledProperty.substring(1);
			return null;
		}

	}

	public static class ProxyException extends Exception {
		private static final long serialVersionUID = -326621728067431728L;

		public ProxyException(final String message, final Throwable cause) {
			super(message, cause);
		}
	}

}
