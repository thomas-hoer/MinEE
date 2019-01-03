package de.minee.hateoes.renderer;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HtmlRenderer extends Renderer {

	private static final Logger logger = Logger.getLogger(HtmlRenderer.class.getName());

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

	private void toHtml(final Object input, final StringBuilder sb) {
		if (input == null) {
			return;
		}
		final Class<?> cls = input.getClass();
		if (String.class.isAssignableFrom(cls)) {
			sb.append("<div>");
			sb.append(input.toString());
			sb.append("</div>");
			return;
		}
		if (cls.isEnum()) {
			sb.append("<div>");
			sb.append(input.toString());
			sb.append("</div>");
			return;
		}

		sb.append("<div class=\"");
		sb.append(cls.getSimpleName());
		sb.append("\">");
		if (Collection.class.isAssignableFrom(cls)) {
			((Collection<?>) input).stream().forEach(o -> toHtml(o, sb));
		} else if (UUID.class.isAssignableFrom(cls) || cls.isPrimitive()) {
			sb.append(input.toString());
		} else {
			for (final Field field : cls.getDeclaredFields()) {
				if (UUID.class.equals(field.getType()) && "id".equals(field.getName())) {
					continue;
				}
				field.setAccessible(true);
				try {
					toHtml(field.get(input), sb);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					logger.log(Level.WARNING,
							"Cannot access field " + field.getType().getSimpleName() + "." + field.getName(), e);
				}
			}
		}
		sb.append("</div>");
	}

	@Override
	public String forCreate(final Class<?> cls) {
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
