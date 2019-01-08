package de.minee.hateoes.renderer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

import de.minee.util.Assertions;
import de.minee.util.ReflectionUtil;

public class JsonRenderer extends Renderer {

	@Override
	public String render(final Object input) {
		final StringBuilder stringBuilder = new StringBuilder();
		toJson(input, stringBuilder);
		return stringBuilder.toString();
	}

	private void toJson(final Object input, final StringBuilder stringBuilder) {
		if (input == null) {
			stringBuilder.append("null");
			return;
		}
		final Class<?> cls = input.getClass();
		if (cls.isPrimitive()) {
			stringBuilder.append(input);
			return;
		}
		if (cls.isArray()) {
			stringBuilder.append('[');
			final StringJoiner stringJoiner = new StringJoiner(",");
			for (int i = 0; i < Array.getLength(input); i++) {
				final Object o = Array.get(input, i);
				stringJoiner.add(render(o));
			}
			stringBuilder.append(stringJoiner.toString());
			stringBuilder.append(']');
			return;
		}
		if (List.class.isAssignableFrom(cls)) {
			stringBuilder.append('[');
			final StringJoiner stringJoiner = new StringJoiner(",");
			for (final Object o : (List<?>) input) {
				stringJoiner.add(render(o));
			}
			stringBuilder.append(stringJoiner.toString());
			stringBuilder.append(']');
			return;
		}
		if (String.class.isAssignableFrom(cls)) {
			stringBuilder.append('"').append(input.toString()).append('"');
			return;
		}
		if (UUID.class.isAssignableFrom(cls)) {
			stringBuilder.append('"').append(input.toString()).append('"');
			return;
		}
		if (cls.isEnum()) {
			stringBuilder.append('"').append(input.toString()).append('"');
			return;
		}
		stringBuilder.append("{");
		final StringJoiner stringJoiner = new StringJoiner(",");
		for (final Field field : ReflectionUtil.getAllFields(cls)) {
			stringJoiner.add(field.getName() + ":" + render(ReflectionUtil.executeGet(field, input)));
		}
		stringBuilder.append(stringJoiner.toString());
		stringBuilder.append("}");

	}

	@Override
	public String forCreate(final Class<?> type) {
		Assertions.assertNotNull(type);
		final Set<Class<?>> knownTypes = new HashSet<>();
		return to(type, knownTypes);
	}

	private String to(final Class<?> type, final Set<Class<?>> knownTypes) {
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
				stringJoiner.add(field.getName() + ":" + to(fieldType, knownTypes));
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
