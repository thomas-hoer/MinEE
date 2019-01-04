package de.minee.cdi;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class CdiUtil {

	private static Map<Class<?>, Object> map = new HashMap<>();

	private CdiUtil() {
	}

	protected static void injectResources(final Object o) {
		final Class<?> clazz = o.getClass();
		if (map.containsKey(clazz)) {
			throw new CdiException("CDI Cycle found");
		}
		for (final Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(Stateless.class)) {
				field.setAccessible(true);
				final Class<?> type = field.getType();
				try {
					field.set(o, getInstance(type));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new CdiException(
							"Cannot inject or instanciate object of type " + type.getSimpleName() + " in field" + field,
							e);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getInstance(final Class<T> cls) {
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
