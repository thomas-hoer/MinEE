package de.minee.cdi;

import de.minee.util.Assertions;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public final class CdiUtil {

	private static Map<Class<?>, Object> map = new HashMap<>();

	private CdiUtil() {
	}

	static void injectResources(final Object o) {
		final Class<?> cls = o.getClass();
		if (map.containsKey(cls)) {
			throw new CdiException("CDI Cycle found");
		}
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
		Assertions.assertNotNull(cls);
		try {
			if (!map.containsKey(cls)) {
				final Object instance = cls.newInstance();
				injectResources(instance);
				map.put(cls, instance);
			}
			return (T) map.get(cls);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new CdiException("Cannot instantiate object of type " + cls.getSimpleName()
					+ ". Probably there is no public constructor with no arguments.", e);
		}
	}
}
