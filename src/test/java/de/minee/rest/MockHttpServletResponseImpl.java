package de.minee.rest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class MockHttpServletResponseImpl implements HttpServletResponse {

	private final StringWriter stringWriter = new StringWriter();
	private int error = 200;
	private final Map<String, String> header = new HashMap<>();

	@Override
	public String getCharacterEncoding() {

		return null;
	}

	@Override
	public String getContentType() {

		return null;
	}

	@Override
	public ServletOutputStream getOutputStream() {

		return null;
	}

	@Override
	public PrintWriter getWriter() {
		return new PrintWriter(stringWriter);
	}

	@Override
	public void setCharacterEncoding(final String charset) {
		// Just a Mock method
	}

	@Override
	public void setContentLength(final int len) {
		// Just a Mock method
	}

	@Override
	public void setContentLengthLong(final long len) {
		// Just a Mock method
	}

	@Override
	public void setContentType(final String type) {
		// Just a Mock method
	}

	@Override
	public void setBufferSize(final int size) {
		// Just a Mock method
	}

	@Override
	public int getBufferSize() {
		return 0;
	}

	@Override
	public void flushBuffer() {
		// Just a Mock method
	}

	@Override
	public void resetBuffer() {
		// Just a Mock method
	}

	@Override
	public boolean isCommitted() {
		return false;
	}

	@Override
	public void reset() {
		// Just a Mock method
	}

	@Override
	public void setLocale(final Locale loc) {
		// Just a Mock method
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	public void addCookie(final Cookie cookie) {
		// Just a Mock method
	}

	@Override
	public boolean containsHeader(final String name) {
		return false;
	}

	@Override
	public String encodeURL(final String url) {
		return null;
	}

	@Override
	public String encodeRedirectURL(final String url) {
		return null;
	}

	@Deprecated
	@Override
	public String encodeUrl(final String url) {
		return null;
	}

	@Deprecated
	@Override
	public String encodeRedirectUrl(final String url) {
		return null;
	}

	@Override
	public void sendError(final int sc, final String msg) {
		error = sc;
	}

	@Override
	public void sendError(final int sc) {
		error = sc;
	}

	@Override
	public void sendRedirect(final String location) {
		// Just a Mock method
	}

	@Override
	public void setDateHeader(final String name, final long date) {
		// Just a Mock method
	}

	@Override
	public void addDateHeader(final String name, final long date) {
		// Just a Mock method
	}

	@Override
	public void setHeader(final String name, final String value) {
		header.put(name, value);
	}

	@Override
	public void addHeader(final String name, final String value) {
		header.put(name, value);
	}

	@Override
	public void setIntHeader(final String name, final int value) {
		// Just a Mock method
	}

	@Override
	public void addIntHeader(final String name, final int value) {
		// Just a Mock method
	}

	@Override
	public void setStatus(final int sc) {
		error = sc;
	}

	@Deprecated
	@Override
	public void setStatus(final int sc, final String sm) {
		// Just a Mock method
	}

	@Override
	public int getStatus() {
		return error;
	}

	@Override
	public String getHeader(final String name) {
		return header.get(name);
	}

	@Override
	public Collection<String> getHeaders(final String name) {
		return header.values();
	}

	@Override
	public Collection<String> getHeaderNames() {

		return header.keySet();
	}

	public String getWrittenOutput() {
		return stringWriter.toString();
	}

	public int getError() {
		return error;
	}
}
