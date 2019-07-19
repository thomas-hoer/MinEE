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

public final class MappingHelper {

	private static final String VARCHAR = "VARCHAR";
	private static final String DOUBLE = "DOUBLE";
	private static final String REAL = "REAL";
	private static final String BIGINT = "BIGINT";
	private static final String INT = "INTEGER";
	private static final String TINYINT = "TINYINT";
	private static final String BOOLEAN = "BOOLEAN";
	private static final String SMALLINT = "SMALLINT";
	private static final String UUID_TYPE = "UUID";
	private static final String DATE_TYPE = "DATETIME";

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
		MAPPED_TYPES.put(Date.class, DATE_TYPE);
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
		if (Integer.class.isAssignableFrom(cls) || int.class.isAssignableFrom(cls)) {
			return Integer.valueOf(object);
		} else if (Long.class.isAssignableFrom(cls) || long.class.isAssignableFrom(cls)) {
			return Long.valueOf(object);
		} else if (Byte.class.isAssignableFrom(cls) || byte.class.isAssignableFrom(cls)) {
			return Byte.valueOf(object);
		} else if (Boolean.class.isAssignableFrom(cls) || boolean.class.isAssignableFrom(cls)) {
			return Boolean.valueOf(object);
		} else if (Short.class.isAssignableFrom(cls) || short.class.isAssignableFrom(cls)) {
			return Short.valueOf(object);
		} else if (Float.class.isAssignableFrom(cls) || float.class.isAssignableFrom(cls)) {
			return Float.valueOf(object);
		} else if (Double.class.isAssignableFrom(cls) || double.class.isAssignableFrom(cls)) {
			return Double.valueOf(object);
		} else if (Character.class.isAssignableFrom(cls) || char.class.isAssignableFrom(cls)) {
			return object.charAt(0);
		} else if (UUID.class.isAssignableFrom(cls)) {
			return UUID.fromString(object);
		} else if (String.class.isAssignableFrom(cls)) {
			return object;
		} else if (Date.class.isAssignableFrom(cls)) {
			return new Date(Long.parseLong(object));
		}
		return null;
	}
	public static Object getDefaultPrimitive(final Class<?> cls) {
		if (int.class.isAssignableFrom(cls)) {
			return Integer.valueOf(0);
		} else if (long.class.isAssignableFrom(cls)) {
			return Long.valueOf(0);
		} else if (byte.class.isAssignableFrom(cls)) {
			return Byte.valueOf((byte)0);
		} else if (boolean.class.isAssignableFrom(cls)) {
			return Boolean.FALSE;
		} else if (short.class.isAssignableFrom(cls)) {
			return Short.valueOf((short)0);
		} else if ( float.class.isAssignableFrom(cls)) {
			return Float.valueOf(0f);
		} else if (double.class.isAssignableFrom(cls)) {
			return Double.valueOf(0d);
		} else if (char.class.isAssignableFrom(cls)) {
			return '\0';
		}
		return null;
	}
}
