package de.minee.util;

import de.minee.util.logging.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javassist.Modifier;

public final class ReflectionUtil {

	private static final String ASSERTION_CLASS_SHOULD_NOT_BE_NULL = "Class should not be null";
	private static final Logger LOGGER = Logger.getLogger(ReflectionUtil.class);

	private ReflectionUtil() {
		// Static Class don't need an instance.
	}

	/**
	 * Get all field values of the source object as key value map. The key is is a
	 * String with the fields name.
	 *
	 * @param source Source Object
	 * @return Key Value Map of all field maps
	 */
	public static Map<String, Object> getAll(final Object source) {
		Assertions.assertNotNull(source, "Can not get fields of null");
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
	 * @param cls Class that should be explored
	 * @return List of all fields
	 */
	public static List<Field> getAllFields(final Class<?> cls) {
		Assertions.assertNotNull(cls, ASSERTION_CLASS_SHOULD_NOT_BE_NULL);

		final Field[] declaredFields = cls.getDeclaredFields();
		// The filter is needed such that the code behave the same with and without the
		// Jacoco plugin
		final List<Field> fields = Arrays.stream(declaredFields)
				.filter(field -> !Modifier.isStatic(field.getModifiers()))
				.filter(field -> !"$jacocoData".equals(field.getName())).collect(Collectors.toList());
		if (!Object.class.equals(cls) && !cls.isEnum() && !cls.isInterface()) {
			fields.addAll(getAllFields(cls.getSuperclass()));
		}
		return fields;
	}

	/**
	 * Returns the Field with name fieldname of the class cls or its parent class.
	 * This includes private and protected fields.
	 *
	 * @param cls       Class that should be explored
	 * @param fieldname Name of the Field
	 * @return Field or null if field does not exist
	 */
	public static Field getDeclaredField(final Class<?> cls, final String fieldname) {
		Assertions.assertNotNull(cls, ASSERTION_CLASS_SHOULD_NOT_BE_NULL);
		Assertions.assertNotEmpty(fieldname, "The fieldname cannot be null or empty String");
		if (Object.class.equals(cls) || cls.isEnum() || cls.isInterface()) {
			return null;
		}
		return Arrays.asList(cls.getDeclaredFields()).stream()
				.filter(field -> fieldname.equalsIgnoreCase(field.getName())).findFirst()
				.orElseGet(() -> getDeclaredField(cls.getSuperclass(), fieldname));
	}

	/**
	 * Returns the Method with name method of the class cls or its parent class. It
	 * only works for methods without arguments. This method only operates on public
	 * instance methods. Private and Protected methods are excluded.
	 *
	 * @param cls        Class that should be explored
	 * @param methodname Name of the Method
	 * @return Method or null if method does not exists
	 */
	public static Method getMethod(final Class<?> cls, final String methodname) {
		Assertions.assertNotNull(cls, ASSERTION_CLASS_SHOULD_NOT_BE_NULL);
		Assertions.assertNotEmpty(methodname, "The methodname cannot be null or empty String");
		if (Object.class.equals(cls) || cls.isEnum() || cls.isInterface()) {
			return null;
		}
		return Arrays.asList(cls.getMethods()).stream().filter(
				method -> method.getParameterTypes().length == 0 && methodname.equalsIgnoreCase(method.getName()))
				.findFirst().orElseGet(() -> getMethod(cls.getSuperclass(), methodname));
	}

	/**
	 * Performs a direct setting into the field without calling a setter method.
	 * base.setField(forSet);
	 *
	 * @param field    Field of base
	 * @param instance Object which field should be injected
	 * @param forSet   Object to be injected into base
	 * @return true if set was successful, false otherwise
	 */
	public static boolean executeSet(final Field field, final Object instance, final Object forSet) {
		Assertions.assertNotNull(field, "Field should not be null");
		Assertions.assertNotNull(instance, "The instance should not be null");
		field.setAccessible(true);
		try {
			field.set(instance, forSet);
			return true;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			LOGGER.warn(e);
		}
		return false;
	}

	/**
	 * Performs a direct get from the field without calling a getter method.
	 * base.getField();
	 *
	 * @param field    Field of object
	 * @param instance Object where value shall be retrieved
	 * @return Value of the field or null if field does not exist
	 */
	public static Object executeGet(final Field field, final Object instance) {
		Assertions.assertNotNull(field, "Field should not be null");
		Assertions.assertNotNull(instance, "The instance should not be null");
		field.setAccessible(true);
		try {
			return field.get(instance);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			LOGGER.warn(e);
			return null;
		}
	}

	public static Object executeGet(final String string, final Object object) {
		final Field field = getDeclaredField(object.getClass(), string);
		return field != null ? executeGet(field, object) : null;
	}

	public static Object invoke(final Method method, final Object object, final Object... args) {
		try {
			return method.invoke(object, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOGGER.warn(e);
			return null;
		}
	}

	/**
	 * Returns a list of all methods of the class and its parent classes. This
	 * includes private and protected methods.
	 *
	 * @param cls Class that should be explored
	 * @return List of all methods
	 */
	public static List<Method> getAllMethods(final Class<?> cls) {
		Assertions.assertNotNull(cls, ASSERTION_CLASS_SHOULD_NOT_BE_NULL);

		final List<Method> methods = new ArrayList<>(Arrays.asList(cls.getDeclaredMethods()));
		if (!Object.class.equals(cls) && !cls.isEnum() && !cls.isInterface()) {
			methods.addAll(getAllMethods(cls.getSuperclass()));
		}
		return methods;
	}

	public static Class<?> getCollectionType(final Field field) {
		final ParameterizedType mapToType = (ParameterizedType) field.getGenericType();
		return (Class<?>) mapToType.getActualTypeArguments()[0];
	}
}
