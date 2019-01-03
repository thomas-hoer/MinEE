package de.minee.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO: search for reflections
//TODO: tests
public class ReflectionUtil {

	private static final Logger logger = Logger.getLogger(ReflectionUtil.class.getName());

	private ReflectionUtil() {
	}

	public static boolean setAll(final Map<String, Object> values, final Object destination) {
		Assertions.assertNotNull(values);
		Assertions.assertNotNull(destination);
		boolean success = true;
		final Class<?> cls = destination.getClass();
		for (final Map.Entry<String, Object> entry : values.entrySet()) {
			final Field field = getDeclaredField(cls, entry.getKey());
			if (field == null) {
				success = false;
			} else {
				try {
					field.set(destination, entry.getValue());
				} catch (IllegalArgumentException | IllegalAccessException e) {
					success = false;
				}
			}
		}
		return success;
	}

	public static Map<String, Object> getAll(final Object source) {
		Assertions.assertNotNull(source);
		final Map<String, Object> result = new HashMap<>();
		final Class<?> cls = source.getClass();
		for (final Field field : getAllFields(cls)) {
			try {
				result.put(field.getName(), field.get(source));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				result.put(field.getName(), null);
			}
		}
		return result;
	}

	public static List<Field> getAllFields(final Class<?> cls) {
		Assertions.assertNotNull(cls);
		final List<Field> fields = new ArrayList<>();
		fields.addAll(Arrays.asList(cls.getDeclaredFields()));
		if (!Object.class.equals(cls)) {
			fields.addAll(getAllFields(cls.getSuperclass()));
		}
		return fields;
	}

	public static Field getDeclaredField(final Class<?> cls, final String fieldname) {
		Assertions.assertNotNull(cls);
		Assertions.assertNotNull(fieldname);
		if (Object.class.equals(cls)) {
			return null;
		}
		return Arrays.asList(cls.getDeclaredFields()).stream()
				.filter(field -> fieldname.equalsIgnoreCase(field.getName())).findFirst()
				.orElseGet(() -> getDeclaredField(cls.getSuperclass(), fieldname));
	}

	/**
	 * Performs a direct setting into the field without calling a setter method.
	 * base.setField(forSet);
	 *
	 * @param field
	 * @param base
	 * @param forSet
	 * @return true if set was successful, false otherwise
	 */
	public static boolean executeSet(final Field field, final Object base, final Object forSet) {
		field.setAccessible(true);
		try {
			field.set(base, forSet);
			return true;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.log(Level.WARNING, "", e);
		}
		return false;
	}

	public static Object executeGet(final Field field, final Object object) {
		field.setAccessible(true);
		try {
			return field.get(object);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.log(Level.WARNING, "", e);
			return null;
		}
	}
}
