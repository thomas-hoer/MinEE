package de.minee.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

public class Cookies {

	private final Map<String, Cookie> cookies = new HashMap<>();
	private final Map<String, Cookie> changedCookies = new HashMap<>();

	public Cookies(final Cookie[] cookies) {
		if (cookies != null) {
			for (final Cookie cookie : cookies) {
				this.cookies.put(cookie.getName(), cookie);
			}
		}
	}

	public String get(final String name) {
		final Cookie cookie = cookies.get(name);
		return cookie != null ? cookie.getValue() : null;

	}

	public void add(final String name, final String value, final int maxAge) {
		final Cookie existingCookie = cookies.get(name);
		final Cookie cookie = existingCookie != null ? existingCookie : new Cookie(name, value);
		cookie.setMaxAge(maxAge);
		changedCookies.put(name, cookie);
		cookies.put(name, cookie);
	}

	public void remove(final String name) {
		final Cookie cookie = cookies.get(name);
		if (cookie != null) {
			cookie.setMaxAge(0);
			changedCookies.put(name, cookie);
		}
	}

	Collection<Cookie> getChangedCookies() {
		return changedCookies.values();
	}

}
