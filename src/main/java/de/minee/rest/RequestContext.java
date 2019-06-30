package de.minee.rest;

import javax.servlet.http.HttpServletRequest;

public class RequestContext {
	private HttpServletRequest request;
	private Cookies cookies;

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(final HttpServletRequest request) {
		this.request = request;
	}

	public Cookies getCookies() {
		return cookies;
	}

	public void setCookies(final Cookies cookies) {
		this.cookies = cookies;
	}

}
