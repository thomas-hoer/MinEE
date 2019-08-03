package de.minee.rest.renderer;

import de.minee.util.Assertions;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

public class JsonRenderer extends AbstractRenderer {

	@Override
	public String render(final Object input) {
		return render(input, new HashSet<>());
	}

	private String render(final Object input, final Set<Object> knownObjects) {
		final StringBuilder stringBuilder = new StringBuilder();
		toJson(input, stringBuilder, knownObjects);
		return stringBuilder.toString();
	}

	private void toJson(final Object input, final StringBuilder stringBuilder, final Set<Object> knownObjects) {
		if (input == null) {
			stringBuilder.append("null");
			return;
		}
		if (knownObjects.contains(input)) {
			toJson(ReflectionUtil.executeGet("id", input), stringBuilder, knownObjects);
			return;
		}
		final Class<?> cls = input.getClass();
		if (cls.isPrimitive() || Boolean.class.isAssignableFrom(cls)) {
			stringBuilder.append(input);
			return;
		} else if (cls.isArray() || List.class.isAssignableFrom(cls)) {
			stringBuilder.append('[');
			final StringJoiner stringJoiner = new StringJoiner(",");
			forEach(input, o -> stringJoiner.add(render(o, knownObjects)));
			stringBuilder.append(stringJoiner.toString());
			stringBuilder.append(']');
			return;
		} else if (isNumber(cls)) {
			stringBuilder.append(escape(input.toString()));
			return;
		} else if (isDirectPrintable(cls)) {
			stringBuilder.append('"').append(escape(input.toString())).append('"');
			return;
		}
		stringBuilder.append("{");
		final StringJoiner stringJoiner = new StringJoiner(",");
		knownObjects.add(input);
		for (final Field field : ReflectionUtil.getAllFields(cls)) {
			final Object fieldObject = ReflectionUtil.executeGet(field, input);
			if (fieldObject != null) {
				stringJoiner.add("\"" + field.getName() + "\":" + render(fieldObject, knownObjects));
			}
		}
		knownObjects.remove(input);
		stringBuilder.append(stringJoiner.toString());
		stringBuilder.append("}");

	}

	@Override
	public String forCreate(final Class<?> cls) {
		Assertions.assertNotNull(cls, "Class should not be null");
		final Set<Class<?>> knownTypes = new HashSet<>();
		return generateTypeDescription(cls, knownTypes);
	}

	private static String generateTypeDescription(final Class<?> type, final Set<Class<?>> knownTypes) {
		if (knownTypes.contains(type)) {
			return "\"" + type.getSimpleName() + "\"";
		}
		knownTypes.add(type);

		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("{");
		final StringJoiner stringJoiner = new StringJoiner(",");
		stringJoiner.add("\"type\":\"" + type.getSimpleName() + "\"");
		for (final Field field : ReflectionUtil.getAllFields(type)) {
			final Class<?> fieldType = field.getType();
			final Field idField = ReflectionUtil.getDeclaredField(fieldType, "id");
			if (fieldType.isArray() || List.class.isAssignableFrom(fieldType)) {
				final Class<?> subType = ReflectionUtil.getCollectionType(field);
				stringJoiner.add("\"" + field.getName() + "\":{\"type\":\"List\",\"of\":"
						+ generateTypeDescription(subType, knownTypes) + "}");
			} else if (fieldType.isEnum()) {
				final StringJoiner values = new StringJoiner(",", "[", "]");
				Arrays.stream(fieldType.getEnumConstants()).map(enumEntry -> String.format("\"%s\"", enumEntry))
						.forEach(values::add);
				stringJoiner.add("\"" + field.getName() + "\":{\"type\":\"Enum\",\"name\":\""
						+ fieldType.getSimpleName() + "\",\"values\":" + values.toString() + "}");
			} else if (idField == null) {
				stringJoiner.add("\"" + field.getName() + "\":{\"type\":\"" + fieldType.getSimpleName() + "\"}");
			} else {
				stringJoiner.add("\"" + field.getName() + "\":" + generateTypeDescription(fieldType, knownTypes));
			}
		}
		stringBuilder.append(stringJoiner.toString());
		stringBuilder.append("}");
		return stringBuilder.toString();
	}

	@Override
	public String forEdit(final Object object) {
		return render(object);
	}

	@Override
	public String getContentType() {
		return "application/json";
	}

}
