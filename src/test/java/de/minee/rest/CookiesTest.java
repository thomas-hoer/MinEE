package de.minee.rest;

import java.util.Collection;

import javax.servlet.http.Cookie;

import org.junit.Assert;
import org.junit.Test;

public class CookiesTest {
	private static final String COOKIE_NAME = "cookie";
	private static final int MAX_AGE = 1000;
	private static final int NEW_MAX_AGE = 1100;
	private static final String USER_ID_VALUE = "U001";
	private static final String USER_ID = "userId";
	private static final String SESSION_ID = "123";
	private static final String NEW_SESSION_ID = "124";
	private static final String SESSION = "session";

	@Test
	public void testNoCookiesAtStart() {
		final Cookies cookies = new Cookies(new Cookie[0]);
		Assert.assertNull(cookies.get(COOKIE_NAME));
		cookies.add(COOKIE_NAME, SESSION_ID, 0);
		Assert.assertNotNull(cookies.get(COOKIE_NAME));
	}

	@Test
	public void testNoCookiesAtStart2() {
		final Cookies cookies = new Cookies(null);
		Assert.assertNull(cookies.get(COOKIE_NAME));
		cookies.add(COOKIE_NAME, SESSION_ID, 0);
		Assert.assertNotNull(cookies.get(COOKIE_NAME));
	}

	@Test
	public void testRemoveCookie() {
		final Cookie cookie1 = new Cookie(SESSION, SESSION_ID);
		cookie1.setMaxAge(MAX_AGE);
		final Cookie cookie2 = new Cookie(USER_ID, USER_ID_VALUE);
		cookie1.setMaxAge(MAX_AGE);
		final Cookies cookies = new Cookies(new Cookie[]{cookie1,cookie2});

		Assert.assertNotNull(cookies.get(SESSION));
		Assert.assertNotNull(cookies.get(USER_ID));

		cookies.remove(USER_ID);
		Assert.assertNotNull(cookies.get(USER_ID));
		final Collection<Cookie> changedCookies = cookies.getChangedCookies();
		Assert.assertEquals(1, changedCookies.size());
		Assert.assertEquals(0, changedCookies.iterator().next().getMaxAge());
	}

	@Test
	public void testRemoveNotExistingCookie() {
		final Cookies cookies = new Cookies(null);
		Assert.assertNull(cookies.get(COOKIE_NAME));
		cookies.remove(COOKIE_NAME);
		final Collection<Cookie> changedCookies = cookies.getChangedCookies();
		Assert.assertEquals(0, changedCookies.size());
	}

	@Test
	public void testUpdateCookie() {
		final Cookie cookie1 = new Cookie(SESSION, SESSION_ID);
		cookie1.setMaxAge(MAX_AGE);
		final Cookie cookie2 = new Cookie(USER_ID, USER_ID_VALUE);
		cookie1.setMaxAge(MAX_AGE);
		final Cookies cookies = new Cookies(new Cookie[]{cookie1,cookie2});

		Assert.assertNotNull(cookies.get(SESSION));
		Assert.assertNotNull(cookies.get(USER_ID));

		cookies.add(SESSION, NEW_SESSION_ID, NEW_MAX_AGE);
		final Collection<Cookie> changedCookies = cookies.getChangedCookies();
		Assert.assertEquals(1, changedCookies.size());
		final Cookie changedCookie = changedCookies.iterator().next();
		Assert.assertEquals(NEW_SESSION_ID, changedCookie.getValue());
		Assert.assertEquals(1100, changedCookie.getMaxAge());
	}}
