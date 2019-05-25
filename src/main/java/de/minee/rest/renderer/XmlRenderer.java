package de.minee.rest.renderer;

import de.minee.util.Assertions;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XmlRenderer extends AbstractRenderer {

	@Override
	public String render(final Object input) {
		if (input == null) {
			return "";
		}
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<" + input.getClass().getSimpleName() + ">");
		toXml(input, stringBuilder, new HashSet<>());
		stringBuilder.append("</" + input.getClass().getSimpleName() + ">");

		return stringBuilder.toString();
	}

	private static void toXml(final Object input, final StringBuilder stringBuilder, final Set<Object> knownObjects) {
		if (input == null) {
			stringBuilder.append("");
			return;
		}
		if (knownObjects.contains(input)) {
			toXml(ReflectionUtil.executeGet("id", input), stringBuilder, knownObjects);
			return;
		}
		final Class<?> cls = input.getClass();
		if (cls.isArray() || List.class.isAssignableFrom(cls)) {
			forEach(input, o -> toXml(o, stringBuilder, knownObjects));
			return;
		}
		if (isDirectPrintable(cls)) {
			stringBuilder.append(input);
			return;
		}
		knownObjects.add(input);
		for (final Field field : ReflectionUtil.getAllFields(cls)) {
			final Object fieldObject = ReflectionUtil.executeGet(field, input);
			if (fieldObject == null) {
				stringBuilder.append("<" + field.getName() + "/>");
			} else if (field.getType().isArray() || List.class.isAssignableFrom(field.getType())) {
				forEach(fieldObject, o -> {
					stringBuilder.append("<" + field.getName() + ">");
					toXml(o, stringBuilder, knownObjects);
					stringBuilder.append("</" + field.getName() + ">");
				});
			} else {
				stringBuilder.append("<" + field.getName() + ">");
				toXml(fieldObject, stringBuilder, knownObjects);
				stringBuilder.append("</" + field.getName() + ">");
			}
		}
		knownObjects.remove(input);
	}

	@Override
	public String forCreate(final Class<?> type) {
		Assertions.assertNotNull(type, "Object type cannot be null");
		final Set<Class<?>> knownTypes = new HashSet<>();
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<" + type.getSimpleName() + ">");
		stringBuilder.append(generateTypeDescription(type, knownTypes));
		stringBuilder.append("</" + type.getSimpleName() + ">");

		return stringBuilder.toString();
	}

	private static String generateTypeDescription(final Class<?> type, final Set<Class<?>> knownTypes) {
		if (knownTypes.contains(type)) {
			return type.getSimpleName();
		}
		knownTypes.add(type);

		final StringBuilder stringBuilder = new StringBuilder();
		for (final Field field : ReflectionUtil.getAllFields(type)) {
			final Class<?> fieldType = field.getType();
			final Field idField = ReflectionUtil.getDeclaredField(fieldType, "id");
			if (idField == null) {
				stringBuilder.append("<" + field.getName() + ">");
				stringBuilder.append(fieldType.getSimpleName());
				stringBuilder.append("</" + field.getName() + ">");
			} else {
				stringBuilder.append("<" + field.getName() + ">");
				stringBuilder.append(generateTypeDescription(fieldType, knownTypes));
				stringBuilder.append("</" + field.getName() + ">");
			}
		}
		return stringBuilder.toString();
	}

	@Override
	public String forEdit(final Object object) {
		return render(object);
	}

	@Override
	public String getContentType() {
		return "application/xml";
	}

}
