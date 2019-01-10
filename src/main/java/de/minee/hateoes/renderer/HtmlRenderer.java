package de.minee.hateoes.renderer;

import de.minee.util.Assertions;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.Collection;
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
			toHtml(input, stringBuilder);
		}
		stringBuilder.append("</body></html>");
		return stringBuilder.toString();
	}

	private static void toHtml(final Object input, final StringBuilder sb) {
		if (input == null) {
			return;
		}
		final Class<?> cls = input.getClass();
		if (String.class.isAssignableFrom(cls)) {
			sb.append(TAG_DIV_START);
			sb.append(input.toString());
			sb.append(TAG_DIV_END);
			return;
		}
		if (cls.isEnum()) {
			sb.append(TAG_DIV_START);
			sb.append(input.toString());
			sb.append(TAG_DIV_END);
			return;
		}

		sb.append(String.format(TAG_DIV_START_CLASS, cls.getSimpleName()));
		if (Collection.class.isAssignableFrom(cls)) {
			((Collection<?>) input).stream().forEach(o -> toHtml(o, sb));
		} else if (UUID.class.isAssignableFrom(cls) || cls.isPrimitive()) {
			sb.append(input.toString());
		} else {
			for (final Field field : cls.getDeclaredFields()) {
				if (UUID.class.equals(field.getType()) && "id".equals(field.getName())) {
					continue;
				}
				toHtml(ReflectionUtil.executeGet(field, input), sb);
			}
		}
		sb.append(TAG_DIV_END);
	}

	@Override
	public String forCreate(final Class<?> cls) {
		Assertions.assertNotNull(cls);
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<!DOCTYPE html><html><body><form method=\"POST\" action=\"create\">");
		for (final Field field : cls.getDeclaredFields()) {
			stringBuilder.append("<input type=\"text\" placeholder=\"").append(field.getName()).append("\" name=\"")
					.append(field.getName()).append("\"><br/>");
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
