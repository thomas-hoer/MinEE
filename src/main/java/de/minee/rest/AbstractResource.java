package de.minee.rest;

import de.minee.cdi.CdiUtil;
import de.minee.rest.parser.Parser;
import de.minee.rest.renderer.HtmlRenderer;
import de.minee.rest.renderer.Renderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

abstract class AbstractResource {

	private final Set<Operation> allowedOperations;
	private final List<Renderer> rendererList = new ArrayList<>();
	private final Map<String, Renderer> rendererMap = new HashMap<>();
	private final List<Parser> parserList = new ArrayList<>();

	AbstractResource(final Operation[] allowedOperations) {
		this.allowedOperations = new HashSet<>(Arrays.asList(allowedOperations));
		if (this.allowedOperations.contains(Operation.ALL)) {
			this.allowedOperations.addAll(Arrays.asList(Operation.values()));
			this.allowedOperations.remove(Operation.ALL);
		}
	}

	/**
	 * Checks weather this resource can be served with this ManagedResource or not.
	 *
	 * @param pathInfo The path that is requestes form the end user.
	 * @return true if the resource can be served. false otherwise.
	 */
	abstract boolean isMatch(String pathInfo);

	/**
	 * Checks if the requested http method is supported by the Resource.
	 *
	 * @param method Requested http method
	 * @return true if the resource can be served. false otherwise.
	 */
	public boolean isMethodAllowed(final String method) {
		return allowedOperations.contains(Operation.valueOf(method));
	}

	abstract void serve(HttpServletRequest req, HttpServletResponse resp) throws IOException;

	public void addRenderer(final Renderer renderer) {
		rendererList.add(renderer);
		rendererMap.put(renderer.getContentType(), renderer);
	}

	public void addParser(final Parser parser) {
		parserList.add(parser);
	}

	Renderer getRenderer(final String contentType) {
		if (rendererList.isEmpty()) {
			addRenderer(CdiUtil.getInstance(HtmlRenderer.class));
		}
		if (contentType != null && rendererMap.containsKey(contentType)) {
			return rendererMap.get(contentType);
		}
		return rendererList.get(0);
	}

	Parser getParser(final String contentType) {
		for (final Parser parser : parserList) {
			if (parser.accept(contentType)) {
				return parser;
			}
		}
		if (!parserList.isEmpty()) {
			return parserList.get(0);
		}
		return null;
	}

}
