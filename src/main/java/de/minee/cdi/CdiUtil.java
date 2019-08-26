package de.minee.cdi;

import de.minee.util.Assertions;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public final class CdiUtil {

	private static final Map<Class<?>, Object> APPLICATION_SCOPED_BEANS = new HashMap<>();

	private CdiUtil() {
	}

	static void injectResources(final Object o) {
		final Class<?> cls = o.getClass();
		for (final Field field : ReflectionUtil.getAllFields(cls)) {
			if (field.isAnnotationPresent(Stateless.class)) {
				ReflectionUtil.executeSet(field, o, getInstance(field.getType()));
			}
		}
	}

	/**
	 * Creates an instance of type cls. The fields of the instance also gets
	 * injected recursively. As of now the utility does not support Interfaces.
	 *
	 * @param cls Class of what a instance should be created
	 * @return Instance of type cls
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getInstance(final Class<T> cls) {
		Assertions.assertNotNull(cls, "Class should not be null");
		try {
			if (!APPLICATION_SCOPED_BEANS.containsKey(cls)) {
				final Constructor<T> constructor = cls.getDeclaredConstructor();
				constructor.setAccessible(true);
				final T instance = constructor.newInstance();
				APPLICATION_SCOPED_BEANS.put(cls, instance);
				injectResources(instance);
			}
			return (T) APPLICATION_SCOPED_BEANS.get(cls);
		} catch (InvocationTargetException | IllegalArgumentException | InstantiationException | IllegalAccessException
				| NoSuchMethodException | SecurityException e) {
			throw new CdiException("Cannot instantiate object of type " + cls.getSimpleName()
					+ ". Probably there is no public constructor with no arguments.", e);
		}
	}
}
