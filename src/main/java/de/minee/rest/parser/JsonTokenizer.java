package de.minee.rest.parser;

import java.util.HashSet;
import java.util.Set;

public class JsonTokenizer {

	private static final Set<Character> TOKENS = new HashSet<>();

	static {
		TOKENS.add('{');
		TOKENS.add('}');
		TOKENS.add('[');
		TOKENS.add(']');
		TOKENS.add(':');
		TOKENS.add(',');
	}

	private final String payload;
	private final int length;
	private int index;
	private String lookup;

	JsonTokenizer(final String payload) {
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
		if (TOKENS.contains(c)) {
			index++;
			lookup = String.valueOf(c);
		} else if (c == '"') {
			parseString();
		} else {
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

	private static boolean allowedLiteral(final char c) {
		final boolean letter = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
		final boolean number = c >= '0' && c <= '9';
		final boolean numberAtithmetic = c == '.' || c == '+' || c == '-';
		return letter || number || numberAtithmetic;
	}
}
