package de.minee.hateoes.parser;

import de.minee.util.Assertions;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JsonParser implements Parser {

	private static final Object JSON_CONTENT_TYPE = "application/json";

	@Override
	public <T> T parse(final String payload, final Class<T> type) throws ParserException {
		final JsonTokenizer tokenizer = new JsonTokenizer(payload);
		try {
			final T result = parse(tokenizer, type);
			Assertions.assertFalse(tokenizer.hasNext(), "Root node is closed but there is still payload left to parse");
			return result;
		} catch (final RuntimeException cause) {
			throw new ParserException(cause);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T parse(final JsonTokenizer tokenizer, final Class<T> type) throws ParserException {
		if ("null".equals(tokenizer.lookup())) {
			tokenizer.next();
			return null;
		}
		if (String.class.isAssignableFrom(type)) {
			return (T) parseString(tokenizer);
		} else if (UUID.class.isAssignableFrom(type)) {
			return (T) UUID.fromString(parseString(tokenizer));
		} else if (type.isArray()) {
			return (T) parseArray(tokenizer, type.getComponentType());
		}
		return parseClass(tokenizer, type);
	}

	private static String parseString(final JsonTokenizer tokenizer) {
		final String token = tokenizer.next();
		final String paranthesisToken;
		if (token.charAt(0) == '"' && token.charAt(token.length() - 1) == '"') {
			paranthesisToken = token.substring(1, token.length() - 1);
		} else {
			paranthesisToken = token;
		}
		return paranthesisToken.replace("\\\\", "\\").replace("\\\"", "\"");
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] parseArray(final JsonTokenizer tokenizer, final Class<T> type) throws ParserException {
		final List<T> list = new ArrayList<>();
		tokenizer.expect("[");
		if (!"]".equals(tokenizer.lookup())) {
			while (tokenizer.hasNext()) {
				list.add(parse(tokenizer, type));
				if (",".equals(tokenizer.lookup())) {
					tokenizer.next();
				} else {
					break;
				}
			}
		}

		tokenizer.expect("]");
		return list.toArray((T[]) Array.newInstance(type, 0));
	}

	private static <T> T parseClass(final JsonTokenizer tokenizer, final Class<T> type) throws ParserException {
		try {
			final T instance = type.newInstance();
			tokenizer.expect("{");
			while (tokenizer.hasNext()) {
				if (!parseProperty(tokenizer, type, instance)) {
					break;
				}
			}
			tokenizer.expect("}");

			return instance;
		} catch (InstantiationException | IllegalAccessException | StringIndexOutOfBoundsException e) {
			throw new ParserException(e);
		}
	}

	private static <T> boolean parseProperty(final JsonTokenizer tokenizer, final Class<T> type, final Object instance)
			throws ParserException {
		if ("}".equals(tokenizer.lookup())) {
			return false;
		}
		final String propertyName = tokenizer.next();
		tokenizer.expect(":");
		final Field propertyField = ReflectionUtil.getDeclaredField(type, propertyName);
		Assertions.assertNotNull(propertyField, "Class " + type + " does not contain a field named " + propertyName);
		ReflectionUtil.executeSet(propertyField, instance, parse(tokenizer, propertyField.getType()));
		if (",".equals(tokenizer.lookup())) {
			tokenizer.next();
		} else {
			return false;
		}
		return true;
	}

	@Override
	public boolean accept(final String contentType) {
		return JSON_CONTENT_TYPE.equals(contentType);
	}

}
