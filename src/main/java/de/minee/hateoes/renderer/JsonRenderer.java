package de.minee.hateoes.renderer;

import de.minee.util.Assertions;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Field;
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
		if (cls.isPrimitive()) {
			stringBuilder.append(input);
			return;
		}
		if (cls.isArray() || List.class.isAssignableFrom(cls)) {
			stringBuilder.append('[');
			final StringJoiner stringJoiner = new StringJoiner(",");
			forEach(input, o -> stringJoiner.add(render(o, knownObjects)));
			stringBuilder.append(stringJoiner.toString());
			stringBuilder.append(']');
			return;
		}
		if (isDirectPrintable(cls)) {
			stringBuilder.append('"').append(input.toString()).append('"');
			return;
		}
		stringBuilder.append("{");
		final StringJoiner stringJoiner = new StringJoiner(",");
		knownObjects.add(input);
		for (final Field field : ReflectionUtil.getAllFields(cls)) {
			final Object fieldObject = ReflectionUtil.executeGet(field, input);
			stringJoiner.add(field.getName() + ":" + render(fieldObject, knownObjects));
		}
		knownObjects.remove(input);
		stringBuilder.append(stringJoiner.toString());
		stringBuilder.append("}");

	}

	@Override
	public String forCreate(final Class<?> type) {
		Assertions.assertNotNull(type);
		final Set<Class<?>> knownTypes = new HashSet<>();
		return generateTypeDescription(type, knownTypes);
	}

	private static String generateTypeDescription(final Class<?> type, final Set<Class<?>> knownTypes) {
		if (knownTypes.contains(type)) {
			return "\"" + type.getSimpleName() + "\"";
		}
		knownTypes.add(type);

		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("{");
		final StringJoiner stringJoiner = new StringJoiner(",");
		stringJoiner.add("Type:\"" + type.getSimpleName() + "\"");
		for (final Field field : ReflectionUtil.getAllFields(type)) {
			final Class<?> fieldType = field.getType();
			final Field idField = ReflectionUtil.getDeclaredField(fieldType, "id");
			if (idField == null) {
				stringJoiner.add(field.getName() + ":\"" + fieldType.getSimpleName() + "\"");
			} else {
				stringJoiner.add(field.getName() + ":" + generateTypeDescription(fieldType, knownTypes));
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

}
