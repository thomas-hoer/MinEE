package de.minee.hateoes.renderer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class Renderer {

	public abstract String render(Object input);

	public abstract String forCreate(Class<?> type);

	public abstract String forEdit(Object object);

	protected String toString(final Entry entry) {
		// Field field, Object object
		final Object fieldObject = entry.getValue();
		if (fieldObject == null) {
			return "";
		}
		final Class<?> cls = fieldObject.getClass();
		try {
			final Field refId = cls.getDeclaredField("id");
			refId.setAccessible(true);
			return refId.get(fieldObject).toString();
		} catch (NoSuchFieldException | SecurityException e) {
			return fieldObject.toString();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return "";
		}
	}

	protected List<Entry> getFields(final Object object) {
		final Class<?> cls = object.getClass();
		final List<Entry> result = new ArrayList<>();
		for (final Field field : cls.getDeclaredFields()) {
			field.setAccessible(true);
			Object value;
			try {
				value = field.get(object);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				value = null;
			}
			final Entry entry = new Entry(field.getName(), value);
			result.add(entry);
		}
		return result;
	}

	class Entry {
		private final String key;
		private final Object value;

		public Entry(final String key, final Object value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public Object getValue() {
			return value;
		}
	}
}
