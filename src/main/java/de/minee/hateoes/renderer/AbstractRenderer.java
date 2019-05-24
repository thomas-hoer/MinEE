package de.minee.hateoes.renderer;

import de.minee.util.ReflectionUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class AbstractRenderer implements Renderer {

	private static final Set<Class<?>> BASE_CLASSES = new HashSet<>();

	static {
		BASE_CLASSES.add(UUID.class);
		BASE_CLASSES.add(String.class);
		BASE_CLASSES.add(Boolean.class);
		BASE_CLASSES.add(Byte.class);
		BASE_CLASSES.add(Character.class);
		BASE_CLASSES.add(Short.class);
		BASE_CLASSES.add(Integer.class);
		BASE_CLASSES.add(Long.class);
		BASE_CLASSES.add(Float.class);
		BASE_CLASSES.add(Double.class);
	}

	protected static boolean isDirectPrintable(final Class<?> cls) {
		return cls.isPrimitive() || cls.isEnum() || isBaseClass(cls);
	}

	protected static boolean isBaseClass(final Class<?> cls) {
		return BASE_CLASSES.contains(cls);
	}

	protected static void forEach(final Object arrayOrCollection, final Consumer<Object> predicate) {
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

	/**
	 * Applies the toString method on the value Object of entry. If the value is an
	 * object with 'id' it will return the String representation of the Id.
	 *
	 * @param entry Value of a Field
	 * @return String representation of the value of entry or its id if available.
	 *         If the value is null it returns an empty String ("").
	 */
	protected String toString(final Entry entry) {
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
		for (final Field field : ReflectionUtil.getAllFields(cls)) {
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
