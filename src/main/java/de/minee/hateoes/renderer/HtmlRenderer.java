package de.minee.hateoes.renderer;

import de.minee.util.Assertions;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HtmlRenderer extends AbstractRenderer {

	private static final String TAG_DIV_START = "<div>";
	private static final String TAG_DIV_END = "</div>";
	private static final String TAG_DIV_START_CLASS = "<div class=\"%s\">";

	@Override
	public String render(final Object input) {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<!DOCTYPE html><html><body>");
		if (input != null) {
			toHtml(input, stringBuilder, new HashSet<>());
		}
		stringBuilder.append("</body></html>");
		return stringBuilder.toString();
	}

	private static void toHtml(final Object input, final StringBuilder stringBuilder, final Set<Object> knownObjects) {
		if (input == null) {
			append(stringBuilder, "");
			return;
		}
		if (knownObjects.contains(input)) {
			append(stringBuilder, ReflectionUtil.executeGet("id", input));
			return;
		}
		final Class<?> cls = input.getClass();
		if (isDirectPrintable(cls)) {
			append(stringBuilder, input.toString());
			return;
		}

		stringBuilder.append(String.format(TAG_DIV_START_CLASS, cls.getSimpleName()));
		if (cls.isArray() || Collection.class.isAssignableFrom(cls)) {
			forEach(input, o -> toHtml(o, stringBuilder, knownObjects));
		} else if (UUID.class.isAssignableFrom(cls) || cls.isPrimitive()) {
			stringBuilder.append(input.toString());
		} else {
			knownObjects.add(input);
			ReflectionUtil.getAllFields(cls).stream()
					.filter(field -> !UUID.class.equals(field.getType()) || !"id".equals(field.getName()))
					.forEach(field -> toHtml(ReflectionUtil.executeGet(field, input), stringBuilder, knownObjects));
			knownObjects.remove(input);
		}
		stringBuilder.append(TAG_DIV_END);
	}

	private static void append(final StringBuilder stringBuilder, final Object content) {
		stringBuilder.append(TAG_DIV_START);
		stringBuilder.append(content);
		stringBuilder.append(TAG_DIV_END);
	}

	@Override
	public String forCreate(final Class<?> cls) {
		Assertions.assertNotNull(cls);
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<!DOCTYPE html><html><body><form method=\"POST\" action=\"create\">");
		for (final Field field : ReflectionUtil.getAllFields(cls)) {
			final String fieldName = field.getName();
			stringBuilder.append("<input type=\"text\" placeholder=\"").append(fieldName).append("\" name=\"")
					.append(fieldName).append("\"><br/>");
		}
		stringBuilder.append("<input type=\"submit\" value=\"Create\"></form></body></html>");
		return stringBuilder.toString();
	}

	@Override
	public String forEdit(final Object object) {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<!DOCTYPE html><html><body><form method=\"POST\" action=\"edit\">");
		for (final Entry entry : getFields(object)) {
			final String value = toString(entry);
			stringBuilder.append("<input type=\"text\" value=\"").append(value).append("\" placeholder=\"")
					.append(entry.getKey()).append("\" name=\"").append(entry.getKey()).append("\"><br/>");
		}
		stringBuilder.append("<input type=\"submit\" value=\"Update\"></form></body></html>");
		return stringBuilder.toString();
	}

}
