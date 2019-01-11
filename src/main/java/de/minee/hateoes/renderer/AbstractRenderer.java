package de.minee.hateoes.renderer;

import de.minee.util.Assertions;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRenderer {

	public abstract String render(Object input);

	public abstract String forCreate(Class<?> type);

	public abstract String forEdit(Object object);

	/**
	 * Applies the toString method on the value Object of entry. If the value is an
	 * object with 'id' it will return the String representation of the Id.
	 *
	 * @param entry
	 * @return String representation of the value of entry or its id if available.
	 *         If the value is null it returns an empty String ("").
	 */
	protected String toString(final Entry entry) {
		Assertions.assertNotNull(entry);
		final Object fieldObject = entry.getValue();
		if (fieldObject == null) {
			return "";
		}
		final Object id = ReflectionUtil.executeGet("id", fieldObject);
		return id != null ? id.toString() : fieldObject.toString();
	}

	protected List<Entry> getFields(final Object object) {
		final Class<?> cls = object.getClass();
		final List<Entry> result = new ArrayList<>();
		for (final Field field : cls.getDeclaredFields()) {
			field.setAccessible(true);
			final Object value = ReflectionUtil.executeGet(field, object);
			final Entry entry = new Entry(field.getName(), value);
			result.add(entry);
		}
		return result;
	}

	static class Entry {
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