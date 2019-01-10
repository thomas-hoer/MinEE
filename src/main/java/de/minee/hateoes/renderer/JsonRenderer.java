package de.minee.hateoes.renderer;

import de.minee.util.Assertions;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Consumer;

public class JsonRenderer extends AbstractRenderer {

	@Override
	public String render(final Object input) {
		final StringBuilder stringBuilder = new StringBuilder();
		toJson(input, stringBuilder);
		return stringBuilder.toString();
	}

	private static void forEach(final Object arrayOrCollection, final Consumer<Object> predicate) {
		if (arrayOrCollection instanceof Collection) {
			for (final Object o : (Collection<?>) arrayOrCollection) {
				predicate.accept(o);
			}
		} else {
			for (int i = 0; i < Array.getLength(arrayOrCollection); i++) {
				final Object o = Array.get(arrayOrCollection, i);
				predicate.accept(o);
			}
		}
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
		if (cls.isArray() || List.class.isAssignableFrom(cls)) {
			stringBuilder.append('[');
			final StringJoiner stringJoiner = new StringJoiner(",");
			forEach(input, o -> stringJoiner.add(render(o)));
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
