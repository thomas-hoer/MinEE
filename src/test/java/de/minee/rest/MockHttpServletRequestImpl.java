package de.minee.rest;

import de.minee.rest.Operation;

import java.io.BufferedReader;
import java.io.StringReader;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

public class MockHttpServletRequestImpl implements HttpServletRequest {

	private final String pathInfo;
	private final String method;
	private final String content;
	private final Map<String, String> parameters = new HashMap<>();

	public MockHttpServletRequestImpl() {
		this(null);
	}

	public MockHttpServletRequestImpl(final String pathInfo) {
		this(pathInfo, Operation.GET, null);
	}

	public MockHttpServletRequestImpl(final String pathInfo, final Operation operation, final String content) {
		this.pathInfo = pathInfo;
		this.method = operation.name();
		this.content = content;
	}

	public void addParameter(final String key, final String value) {
		this.parameters.put(key, value);
	}

	public void addParameters(final Map<String, String> parameters) {
		this.parameters.putAll(parameters);
	}

	@Override
	public Object getAttribute(final String name) {
		return null;
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return null;
	}

	@Override
	public String getCharacterEncoding() {
		return null;
	}

	@Override
	public void setCharacterEncoding(final String env) {
		// Just a Mock method
	}

	@Override
	public int getContentLength() {
		return 0;
	}

	@Override
	public long getContentLengthLong() {
		return 0;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public ServletInputStream getInputStream() {
		return null;
	}

	@Override
	public String getParameter(final String name) {
		return parameters.get(name);
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return null;
	}

	@Override
	public String[] getParameterValues(final String name) {
		return null;
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return null;
	}

	@Override
	public String getProtocol() {
		return null;
	}

	@Override
	public String getScheme() {

		return null;
	}

	@Override
	public String getServerName() {

		return null;
	}

	@Override
	public int getServerPort() {

		return 0;
	}

	@Override
	public BufferedReader getReader() {
		return new BufferedReader(new StringReader(content));
	}

	@Override
	public String getRemoteAddr() {

		return null;
	}

	@Override
	public String getRemoteHost() {

		return null;
	}

	@Override
	public void setAttribute(final String name, final Object o) {
		// Just a Mock method
	}

	@Override
	public void removeAttribute(final String name) {
		// Just a Mock method
	}

	@Override
	public Locale getLocale() {

		return null;
	}

	@Override
	public Enumeration<Locale> getLocales() {

		return null;
	}

	@Override
	public boolean isSecure() {

		return false;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(final String path) {

		return null;
	}

	@Deprecated
	@Override
	public String getRealPath(final String path) {
		return null;
	}

	@Override
	public int getRemotePort() {

		return 0;
	}

	@Override
	public String getLocalName() {

		return null;
	}

	@Override
	public String getLocalAddr() {

		return null;
	}

	@Override
	public int getLocalPort() {

		return 0;
	}

	@Override
	public ServletContext getServletContext() {

		return null;
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {

		return null;
	}

	@Override
	public AsyncContext startAsync(final ServletRequest servletRequest, final ServletResponse servletResponse)
			throws IllegalStateException {

		return null;
	}

	@Override
	public boolean isAsyncStarted() {

		return false;
	}

	@Override
	public boolean isAsyncSupported() {

		return false;
	}

	@Override
	public AsyncContext getAsyncContext() {

		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {

		return null;
	}

	@Override
	public String getAuthType() {

		return null;
	}

	@Override
	public Cookie[] getCookies() {

		return null;
	}

	@Override
	public long getDateHeader(final String name) {

		return 0;
	}

	@Override
	public String getHeader(final String name) {

		return null;
	}

	@Override
	public Enumeration<String> getHeaders(final String name) {

		return null;
	}

	@Override
	public Enumeration<String> getHeaderNames() {

		return null;
	}

	@Override
	public int getIntHeader(final String name) {

		return 0;
	}

	@Override
	public String getMethod() {

		return method;
	}

	@Override
	public String getPathInfo() {
		return pathInfo;
	}

	@Override
	public String getPathTranslated() {

		return null;
	}

	@Override
	public String getContextPath() {

		return null;
	}

	@Override
	public String getQueryString() {

		return null;
	}

	@Override
	public String getRemoteUser() {

		return null;
	}

	@Override
	public boolean isUserInRole(final String role) {

		return false;
	}

	@Override
	public Principal getUserPrincipal() {

		return null;
	}

	@Override
	public String getRequestedSessionId() {

		return null;
	}

	@Override
	public String getRequestURI() {

		return null;
	}

	@Override
	public StringBuffer getRequestURL() {

		return null;
	}

	@Override
	public String getServletPath() {

		return null;
	}

	@Override
	public HttpSession getSession(final boolean create) {

		return null;
	}

	@Override
	public HttpSession getSession() {

		return null;
	}

	@Override
	public String changeSessionId() {

		return null;
	}

	@Override
	public boolean isRequestedSessionIdValid() {

		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {

		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {

		return false;
	}

	@Deprecated
	@Override
	public boolean isRequestedSessionIdFromUrl() {

		return false;
	}

	@Override
	public boolean authenticate(final HttpServletResponse response) {

		return false;
	}

	@Override
	public void login(final String username, final String password) {
		// Just a Mock method
	}

	@Override
	public void logout() {
		// Just a Mock method
	}

	@Override
	public Collection<Part> getParts() {
		return null;
	}

	@Override
	public Part getPart(final String name) {
		return null;
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(final Class<T> handlerClass) {
		return null;
	}

}
