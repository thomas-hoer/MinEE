package de.minee.jpa;

import de.minee.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Function;

public final class MappingHelper {

	private static final String VARCHAR = "VARCHAR";
	private static final String DOUBLE = "DOUBLE";
	private static final String REAL = "REAL";
	private static final String BIGINT = "BIGINT";
	private static final String INT = "INTEGER";
	private static final String TINYINT = "TINYINT";
	private static final String BOOLEAN = "BOOLEAN";
	private static final String SMALLINT = "SMALLINT";
	private static final String CHAR = "CHAR(1)";
	private static final String UUID_TYPE = "UUID";
	private static final String DATE_TYPE = "TIMESTAMP";

	private static final Map<Class<?>, String> MAPPED_TYPES = new HashMap<>();
	private static final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
	private static final Map<Class<?>, Function<String, Object>> PARSE_FUNCTION = new HashMap<>();

	static {
		MAPPED_TYPES.put(Boolean.class, BOOLEAN);
		MAPPED_TYPES.put(Character.class, CHAR);
		MAPPED_TYPES.put(Byte.class, TINYINT);
		MAPPED_TYPES.put(Short.class, SMALLINT);
		MAPPED_TYPES.put(Integer.class, INT);
		MAPPED_TYPES.put(Long.class, BIGINT);
		MAPPED_TYPES.put(Float.class, REAL);
		MAPPED_TYPES.put(Double.class, DOUBLE);
		MAPPED_TYPES.put(String.class, VARCHAR);
		MAPPED_TYPES.put(boolean.class, BOOLEAN);
		MAPPED_TYPES.put(char.class, CHAR);
		MAPPED_TYPES.put(byte.class, TINYINT);
		MAPPED_TYPES.put(short.class, SMALLINT);
		MAPPED_TYPES.put(int.class, INT);
		MAPPED_TYPES.put(long.class, BIGINT);
		MAPPED_TYPES.put(float.class, REAL);
		MAPPED_TYPES.put(double.class, DOUBLE);
		MAPPED_TYPES.put(UUID.class, UUID_TYPE);
		MAPPED_TYPES.put(Date.class, DATE_TYPE);

		PARSE_FUNCTION.put(Integer.class, Integer::valueOf);
		PARSE_FUNCTION.put(int.class, Integer::valueOf);
		PARSE_FUNCTION.put(Long.class, Long::valueOf);
		PARSE_FUNCTION.put(long.class, Long::valueOf);
		PARSE_FUNCTION.put(Byte.class, Byte::valueOf);
		PARSE_FUNCTION.put(byte.class, Byte::valueOf);
		PARSE_FUNCTION.put(Boolean.class, Boolean::valueOf);
		PARSE_FUNCTION.put(boolean.class, Boolean::valueOf);
		PARSE_FUNCTION.put(Short.class, Short::valueOf);
		PARSE_FUNCTION.put(short.class, Short::valueOf);
		PARSE_FUNCTION.put(Float.class, Float::valueOf);
		PARSE_FUNCTION.put(float.class, Float::valueOf);
		PARSE_FUNCTION.put(Double.class, Double::valueOf);
		PARSE_FUNCTION.put(double.class, Double::valueOf);
		final Function<String, Object> charAt0 = s -> s.charAt(0);
		PARSE_FUNCTION.put(Character.class, charAt0);
		PARSE_FUNCTION.put(char.class, charAt0);
		PARSE_FUNCTION.put(UUID.class, UUID::fromString);
		PARSE_FUNCTION.put(String.class, s -> s);
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
		if (clazz.isEnum()) {
			final StringJoiner stringJoiner = new StringJoiner(",", "(", ")");
			Arrays.stream(clazz.getEnumConstants()).forEach(value -> stringJoiner.add("'" + value.toString() + "'"));
			return "ENUM" + stringJoiner.toString();
		}
		final Field childId = ReflectionUtil.getDeclaredField(clazz, "id");
		if (childId == null) {
			throw new MappingException(
					"Not supported field type: " + clazz.getSimpleName() + " in Class " + field.getDeclaringClass());
		}
		return childId.getType().getSimpleName();
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
		if (Date.class.isAssignableFrom(clazz)) {
			return object;
		}
		final UUID id = (UUID) ReflectionUtil.executeGet("id", object);
		if (id == null) {
			return NULL_UUID;
		}
		return id;
	}

	public static UUID getId(final Object object) {
		return (UUID) ReflectionUtil.executeGet("id", object);
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

	public static Object parseType(final String object, final Class<?> cls) {
		if (PARSE_FUNCTION.containsKey(cls)) {
			return PARSE_FUNCTION.get(cls).apply(object);
		} else if (Date.class.isAssignableFrom(cls)) {
			return new Date(Long.parseLong(object));
		} else {
			return null;
		}
	}

	public static Object getDefaultPrimitive(final Class<?> cls) {
		if (int.class.isAssignableFrom(cls)) {
			return 0;
		} else if (long.class.isAssignableFrom(cls)) {
			return 0l;
		} else if (byte.class.isAssignableFrom(cls)) {
			return (byte) 0;
		} else if (boolean.class.isAssignableFrom(cls)) {
			return false;
		} else if (short.class.isAssignableFrom(cls)) {
			return (short) 0;
		} else if (float.class.isAssignableFrom(cls)) {
			return 0f;
		} else if (double.class.isAssignableFrom(cls)) {
			return 0d;
		} else if (char.class.isAssignableFrom(cls)) {
			return '\0';
		}
		return null;
	}
}
