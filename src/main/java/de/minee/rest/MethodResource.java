package de.minee.rest;

import de.minee.rest.parser.Parser;
import de.minee.rest.parser.ParserException;
import de.minee.rest.path.ParameterizedPath;
import de.minee.rest.renderer.Renderer;
import de.minee.util.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class MethodResource extends AbstractResource {

	private static final Logger LOGGER = Logger.getLogger(MethodResource.class);

	private final ParameterizedPath path;
	private final RestServlet instance;
	private final Method method;
	private final List<ArgumentMapping> methodParams = new ArrayList<>();

	MethodResource(final String fullPath, final Operation[] allowedOperations, final RestServlet instance,
			final Method method) {
		super(allowedOperations);
		path = new ParameterizedPath(fullPath);
		this.method = method;
		this.instance = instance;
		final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		final Class<?>[] annotatedTypes = method.getParameterTypes();
		for (int i = 0; i < annotatedTypes.length; i++) {
			final PathParam pathParam = getAnnotation(parameterAnnotations[i], PathParam.class);
			final ArgumentMapping mapping;
			if (pathParam != null) {
				mapping = new PathParamMapping(path, pathParam.value());
			} else {
				mapping = new PayloadMapping(annotatedTypes[i]);
			}
			methodParams.add(mapping);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends Annotation> T getAnnotation(final Annotation[] annotations,
			final Class<T> annotationType) {
		for (final Annotation annotation : annotations) {
			if (annotationType.isInstance(annotation)) {
				return (T) annotation;
			}
		}
		return null;
	}

	private interface ArgumentMapping {
		Object map(HttpServletRequest req);
	}

	private class PayloadMapping implements ArgumentMapping {

		private final Class<?> type;

		public PayloadMapping(final Class<?> type) {
			this.type = type;
		}

		@Override
		public Object map(final HttpServletRequest req) {
			final Parser parser = getParser(req.getContentType());
			try {
				final String input = req.getReader().lines().collect(Collectors.joining());
				return parser.parse(input, type);
			} catch (ParserException | IOException e) {
				throw new MappingException(e.getMessage(), e);
			}
		}
	}

	private static class MappingException extends RuntimeException {
		public MappingException(final String message, final Throwable cause) {
			super(message, cause);
		}
	}

	private static class PathParamMapping implements ArgumentMapping {
		private final int pathNr;

		public PathParamMapping(final ParameterizedPath path, final String pathParam) {
			pathNr = path.paramPos(pathParam);
		}

		@Override
		public Object map(final HttpServletRequest req) {
			final String[] paths = req.getPathInfo().split("/");
			return paths[pathNr];
		}
	}

	@Override
	public boolean isMatch(final String pathInfo) {
		return path.isMatch(pathInfo);
	}

	@Override
	public void serve(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		try {
			final Object[] args = methodParams.stream().map(param -> param.map(req)).toArray();
			Object result = null;
			result = this.method.invoke(this.instance, args);
			try (final PrintWriter writer = resp.getWriter()) {
				final Renderer renderer = getRenderer().get(0);
				resp.setContentType(renderer.getContentType());
				writer.write(renderer.render(result));
			}
		} catch (IllegalAccessException | IllegalArgumentException e) {
			resp.getWriter().write(e.getMessage());
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			LOGGER.warn("Not able to invoke method " + method, e);
		} catch (final InvocationTargetException e) {
			resp.getWriter().write(e.getTargetException().getMessage());
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			LOGGER.warn("Exception thrown during " + method, e);
		} catch (final MappingException e) {
			resp.getWriter().write(e.getMessage());
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			LOGGER.warn("Cannot parse user Input", e);
		}

	}

	@Override
	public String toString() {
		return path.toString();
	}
}
