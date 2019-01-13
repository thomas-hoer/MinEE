package de.minee.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class ReflectionUtil {

	private static final Logger LOGGER = Logger.getLogger(ReflectionUtil.class.getName());

	private ReflectionUtil() {
		// Static Class don't need an instance.
	}

	/**
	 *
	 * @param values
	 * @param destination
	 * @return
	 */
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
				executeSet(field, destination, entry.getValue());
			}
		}
		return success;
	}

	/**
	 *
	 * @param source
	 * @return
	 */
	public static Map<String, Object> getAll(final Object source) {
		Assertions.assertNotNull(source);
		final Map<String, Object> result = new HashMap<>();
		final Class<?> cls = source.getClass();
		for (final Field field : getAllFields(cls)) {
			field.setAccessible(true);
			result.put(field.getName(), executeGet(field, source));
		}
		return result;
	}

	/**
	 * Returns a list of all fields of the class and its parent classes. This
	 * includes private and protected fields.
	 *
	 * @param cls Class that should explored
	 * @return List of all fields
	 */
	public static List<Field> getAllFields(final Class<?> cls) {
		Assertions.assertNotNull(cls);

		final Field[] declaredFields = cls.getDeclaredFields();
		// The filter is needed such that the code behave the same with and without the
		// Jacoco plugin
		final List<Field> fields = Arrays.stream(declaredFields).filter(field -> !"$jacocoData".equals(field.getName()))
				.collect(Collectors.toList());
		if (!Object.class.equals(cls) && !cls.isEnum() && !cls.isInterface()) {
			fields.addAll(getAllFields(cls.getSuperclass()));
		}
		return fields;
	}

	/**
	 *
	 * @param cls
	 * @param fieldname
	 * @return
	 */
	public static Field getDeclaredField(final Class<?> cls, final String fieldname) {
		Assertions.assertNotNull(cls);
		Assertions.assertNotNull(fieldname);
		if (Object.class.equals(cls) || cls.isEnum() || cls.isInterface()) {
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
	 * @param field  Field of base
	 * @param base   Object which field should be injected
	 * @param forSet Object to be injected into base
	 * @return true if set was successful, false otherwise
	 */
	public static boolean executeSet(final Field field, final Object base, final Object forSet) {
		Assertions.assertNotNull(field);
		Assertions.assertNotNull(base);
		field.setAccessible(true);
		try {
			field.set(base, forSet);
			return true;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
		return false;
	}

	/**
	 *
	 * @param field
	 * @param object
	 * @return
	 */
	public static Object executeGet(final Field field, final Object object) {
		Assertions.assertNotNull(field);
		Assertions.assertNotNull(object);
		field.setAccessible(true);
		try {
			return field.get(object);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			LOGGER.log(Level.WARNING, "", e);
			return null;
		}
	}

	public static Object executeGet(final String string, final Object object) {
		final Field field = getDeclaredField(object.getClass(), string);
		return field != null ? executeGet(field, object) : null;
	}
}
