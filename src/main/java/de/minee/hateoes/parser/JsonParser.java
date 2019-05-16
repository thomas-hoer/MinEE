package de.minee.hateoes.parser;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.minee.util.Assertions;
import de.minee.util.ReflectionUtil;

public class JsonParser implements Parser {

	@Override
	public <T> T parse(final String payload, final Class<T> type) throws ParserException {
		final Tokenizer tokenizer = new Tokenizer(payload);
		final T result = parse(tokenizer, type);
		Assertions.assertFalse(tokenizer.hasNext(), "Root node is closed but there is still payload left to parse");
		return result;
	}

	private <T> T parse(final Tokenizer tokenizer, final Class<T> type) throws ParserException {
		if (String.class.isAssignableFrom(type)) {
			return (T) parseString(tokenizer);
		} else if (UUID.class.isAssignableFrom(type)) {
			return (T) UUID.fromString(parseString(tokenizer));
		} else if (type.isArray()) {
			return (T) parseArray(tokenizer, type.getComponentType());
		}
		return parseClass(tokenizer, type);
	}

	private String parseString(final Tokenizer tokenizer) throws ParserException {
		final String token = tokenizer.next();
		final String paranthesisToken;
		if (token.charAt(0) == '"' && token.charAt(token.length() - 1) == '"') {
			paranthesisToken = token.substring(1, token.length() - 1);
		} else {
			paranthesisToken = token;
		}
		return paranthesisToken.replace("\\\\", "\\").replace("\\\"", "\"");
	}

	private <T> T[] parseArray(final Tokenizer tokenizer, final Class<T> type) throws ParserException {
		final List<T> list = new ArrayList<>();
		tokenizer.expect("[");
		while (tokenizer.hasNext()) {
			if ("]".equals(tokenizer.lookup())) {
				break;
			}
			list.add(parse(tokenizer, type));
			if (",".equals(tokenizer.lookup())) {
				tokenizer.next();
				continue;
			}
			break;
		}
		tokenizer.expect("]");
		return list.toArray((T[]) Array.newInstance(type, 0));
	}

	private <T> T parseClass(final Tokenizer tokenizer, final Class<T> type) throws ParserException {
		try {
			final T instance = type.newInstance();
			tokenizer.expect("{");
			while (tokenizer.hasNext()) {
				if ("}".equals(tokenizer.lookup())) {
					break;
				}
				final String propertyName = tokenizer.next();
				tokenizer.expect(":");
				final Field propertyField = ReflectionUtil.getDeclaredField(type, propertyName);
				Assertions.assertNotNull(propertyField,
						"Class " + type + " does not contain a field named " + propertyName);
				ReflectionUtil.executeSet(propertyField, instance, parse(tokenizer, propertyField.getType()));
				if (",".equals(tokenizer.lookup())) {
					tokenizer.next();
					continue;
				}
				break;
			}
			tokenizer.expect("}");

			return instance;
		} catch (InstantiationException | IllegalAccessException | StringIndexOutOfBoundsException e) {
			throw new ParserException(e);
		}
	}

	private class Tokenizer {
		private final String payload;
		private final int length;
		private int index = 0;
		private String lookup;

		Tokenizer(final String payload) {
			this.payload = payload.trim();
			this.length = this.payload.length();
		}

		boolean hasNext() {
			return index < length;
		}

		String next() {
			lookup();
			final String result = lookup;
			lookup = null;
			return result;
		}

		void expect(final String expected) throws ParserException {
			if (!expected.equals(lookup())) {
				throw new ParserException("'" + expected + "' expected but " + lookup + " found at pos " + index);
			}
			next();
		}

		String lookup() {
			if (lookup != null) {
				return lookup;
			}
			if (!hasNext()) {
				return null;
			}
			char c = payload.charAt(index);
			while (Character.isWhitespace(c)) {
				c = payload.charAt(++index);
			}
			switch (c) {
			case '{':
			case '}':
			case '[':
			case ']':
			case ':':
			case ',':
				index++;
				lookup = String.valueOf(c);
				break;
			case '"':
				parseString();
				break;
			default:
				parseConstant();
			}

			return lookup;
		}

		private void parseConstant() {
			final int startIndex = index;
			while (true) {
				final char charAtI = payload.charAt(++index);
				if (!allowedLiteral(charAtI) || index + 1 == length) {
					lookup = payload.substring(startIndex, index);
					break;
				}
			}
		}

		private void parseString() {
			final int startIndex = index;
			while (true) {
				final char charAtI = payload.charAt(++index);
				if (charAtI == '"' || index + 1 == length) {
					lookup = payload.substring(startIndex, ++index);
					break;
				}
				if (charAtI == '\\') {
					index++;
				}
			}
		}

		private boolean allowedLiteral(final char c) {
			return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '.';
		}
	}
}
