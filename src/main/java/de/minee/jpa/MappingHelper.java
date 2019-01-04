package de.minee.jpa;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

class MappingHelper {

	private static final String VARCHAR = "VARCHAR";
	private static final String DOUBLE = "DOUBLE";
	private static final String REAL = "REAL";
	private static final String BIGINT = "BIGINT";
	private static final String INT = "INT";
	private static final String TINYINT = "TINYINT";
	private static final String BOOLEAN = "BOOLEAN";
	private static final String SMALLINT = "SMALLINT";
	private static final String UUID_TYPE = "UUID";

	private static final Map<Class<?>, String> MAPPED_TYPES = new HashMap<>();
	private static final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

	static {
		MAPPED_TYPES.put(Boolean.class, BOOLEAN);
		MAPPED_TYPES.put(Character.class, SMALLINT);
		MAPPED_TYPES.put(Byte.class, TINYINT);
		MAPPED_TYPES.put(Short.class, SMALLINT);
		MAPPED_TYPES.put(Integer.class, INT);
		MAPPED_TYPES.put(Long.class, BIGINT);
		MAPPED_TYPES.put(Float.class, REAL);
		MAPPED_TYPES.put(Double.class, DOUBLE);
		MAPPED_TYPES.put(String.class, VARCHAR);
		MAPPED_TYPES.put(boolean.class, BOOLEAN);
		MAPPED_TYPES.put(char.class, SMALLINT);
		MAPPED_TYPES.put(byte.class, TINYINT);
		MAPPED_TYPES.put(short.class, SMALLINT);
		MAPPED_TYPES.put(int.class, INT);
		MAPPED_TYPES.put(long.class, BIGINT);
		MAPPED_TYPES.put(float.class, REAL);
		MAPPED_TYPES.put(double.class, DOUBLE);
		MAPPED_TYPES.put(UUID.class, UUID_TYPE);
	}

	private MappingHelper() {
	}

	static String mapDatabaseType(final Field field) {
		final Class<?> clazz = field.getType();
		final String mappedType = MAPPED_TYPES.get(clazz);
		if (mappedType != null) {
			return mappedType;
		}
		if (List.class.isAssignableFrom(clazz)) {
			return null;
		}
		if (clazz.isArray()) {
			if (!clazz.getComponentType().isPrimitive()) {
				return "ARRAY";
			} else if (byte.class.equals(clazz.getComponentType())) {
				return VARCHAR;
			} else {
				throw new MappingException("Not supported field type: " + clazz.getSimpleName() + " in Class "
						+ field.getDeclaringClass());
			}
		}
		try {
			if (clazz.isEnum()) {
				final StringJoiner stringJoiner = new StringJoiner(",", "(", ")");
				Arrays.stream(clazz.getEnumConstants())
						.forEach(value -> stringJoiner.add("'" + value.toString() + "'"));
				return "ENUM" + stringJoiner.toString();
			}
			final Field childId = clazz.getDeclaredField("id");
			return childId.getType().getSimpleName();
		} catch (final NoSuchFieldException e) {
			throw new MappingException(
					"Not supported field type: " + clazz.getSimpleName() + " in Class " + field.getDeclaringClass());
		}
	}

	public static Object getDbObject(final Object object) {
		if (object == null) {
			return null;
		}
		final Class<?> clazz = object.getClass();
		if (clazz.isEnum()) {
			return object.toString();
		}
		final String mappedType = MAPPED_TYPES.get(clazz);
		if (mappedType != null) {
			return object;
		}
		if (clazz.isArray()) {
			if (byte.class.equals(clazz.getComponentType())) {
				return Base64.getEncoder().encodeToString((byte[]) object);
			}
			return object;
		}
		if (List.class.isAssignableFrom(clazz)) {
			return null;
		}
		try {
			final Field childId = clazz.getDeclaredField("id");
			childId.setAccessible(true);
			final UUID id = (UUID) childId.get(object);
			if (id == null) {
				return NULL_UUID;
			}
			return id;
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			throw new MappingException("No Field id found in Class " + clazz.getName(), e);
		}
	}

	public static UUID getId(final Object object) {
		try {
			final Class<?> clazz = object.getClass();
			final Field childId = clazz.getDeclaredField("id");
			childId.setAccessible(true);
			return (UUID) childId.get(object);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			return null;
		}
	}

	public static Object getEnum(final Class<?> cls, final String enumString) {
		return Arrays.stream(cls.getEnumConstants()).filter(c -> enumString.equals(c.toString())).findFirst()
				.orElse(null);
	}

	public static boolean isSupportedType(final Class<?> cls) {
		return MAPPED_TYPES.containsKey(cls);
	}

	public static String mapType(final Class<?> cls) {
		return MAPPED_TYPES.get(cls);
	}
}
