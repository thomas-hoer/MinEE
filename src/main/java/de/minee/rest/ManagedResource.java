package de.minee.rest;

import de.minee.jpa.AbstractDAO;
import de.minee.jpa.DatabaseException;
import de.minee.rest.parser.Parser;
import de.minee.rest.parser.ParserException;
import de.minee.rest.path.ManagedPath;
import de.minee.rest.renderer.Renderer;
import de.minee.util.Logger;
import de.minee.util.ReflectionUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class ManagedResource<T> extends AbstractResource {

	private static final String UTF_8 = "UTF-8";

	private static final Logger LOGGER = Logger.getLogger(ManagedResource.class);

	private final ManagedPath<T> path;
	private final Class<T> type;
	private AbstractDAO dao;

	/**
	 * Creates a new fully managed resource for a REST interface. It can handle GET
	 * for retrieving, PUT and POST for creating and updating resources as well es
	 * DELETE for deleting existing resources.
	 *
	 * @param context           Context containing information of other resources
	 *                          managed by the servlet
	 * @param fullPath          full path of the resource based on the servlet root
	 * @param allowedOperations http methods that are allowed to handle
	 * @param type              The type of which objects will be handled by this
	 *                          resource path
	 */
	ManagedResource(final HateoesContext context, final String fullPath, final Operation[] allowedOperations,
			final Class<T> type) {
		super(allowedOperations);
		path = new ManagedPath<>(context, fullPath, type);
		this.type = type;

	}

	public void setDao(final AbstractDAO dao) {
		this.dao = dao;
	}

	@Override
	public boolean isMatch(final String pathInfo) {
		return path.isMatch(pathInfo);
	}

	@Override
	public void serve(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		final String pathInfo = req.getPathInfo();
		final String[] pathSplit = pathInfo.split("/");
		final List<?> result;
		try {
			if (pathSplit.length == path.size() + 1) {
				final String action = pathSplit[path.size()];
				if ("create".equals(action)) {
					serveCreate(req, resp);
					return;
				} else if ("edit".equals(action)) {
					serveEdit(req, resp, pathSplit);
					return;
				}
				resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
				return;
			}
			result = getSelectedResource(pathSplit);
		} catch (final DatabaseException e) {
			LOGGER.error("Cannot access database or database entities", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		if (result.isEmpty()) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		resp.setCharacterEncoding(UTF_8);
		try (final PrintWriter writer = resp.getWriter()) {
			final Renderer renderer = getRenderer(req.getContentType());
			resp.setContentType(renderer.getContentType());
			if (result.size() == 1) {
				writer.write(renderer.render(result.get(0)));
			} else {
				writer.write(renderer.render(result));
			}
		}
	}

	private void serveEdit(final HttpServletRequest req, final HttpServletResponse resp, final String[] pathSplit)
			throws IOException {
		final String method = req.getMethod();
		final List<T> result = getSelectedResource(pathSplit);
		if (result.isEmpty()) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else if (result.size() != 1) {
			resp.sendError(HttpServletResponse.SC_CONFLICT);
		} else {
			if (method.equals(Operation.GET.name())) {
				doGetEdit(result.get(0), req, resp);
			} else if (method.equals(Operation.POST.name())) {
				doPostEdit(req, resp);
			}
		}
	}

	private void serveCreate(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		final String method = req.getMethod();
		if (method.equals(Operation.GET.name())) {
			doGetCreate(req, resp);
		} else if (method.equals(Operation.POST.name())) {
			doPostCreate(req, resp);
		}
	}

	private List<T> getSelectedResource(final String[] pathSplit) {
		return path.executeSelect(pathSplit, dao.select(type));
	}

	private Object fromString(final Class<?> fieldType, final String parameter) {
		if (parameter == null) {
			return null;
		}
		if (int.class.equals(fieldType) || Integer.class.equals(fieldType)) {
			return Integer.valueOf(parameter);
		}
		if (String.class.equals(fieldType)) {
			return parameter;
		}
		if (UUID.class.equals(fieldType)) {
			return "".equals(parameter) ? null : UUID.fromString(parameter);
		}

		final Field refId = ReflectionUtil.getDeclaredField(fieldType, "id");
		if (refId != null) {
			if ("".equals(parameter)) {
				return null;
			}
			final UUID uuid = UUID.fromString(parameter);
			try {
				final Object result = dao.select(fieldType).byId(uuid);
				if (result == null) {
					throw new RestException(fieldType.getSimpleName() + " with Id " + parameter + " not found");
				}
				return result;
			} catch (final DatabaseException e) {
				throw new RestException("Cannot translate String to type " + fieldType.getSimpleName(), e);
			}
		}
		throw new RestException("Not able to map String to type " + fieldType.getSimpleName());
	}

	private interface ManagedResourceConsumer {
		String accept(Object instance);
	}

	private void assembleRequestObject(final HttpServletRequest req, final HttpServletResponse resp,
			final ManagedResourceConsumer consumer) throws IOException {
		try {
			final Parser bestFitParser = getParser(req.getContentType());
			final Object instance = createRequestedInstance(req, resp, bestFitParser);
			if (instance != null) {
				resp.getWriter().write(consumer.accept(instance));
			}
		} catch (InstantiationException | IllegalAccessException | DatabaseException | IOException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new IOException(e);
		} catch (final RuntimeException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			throw e;
		}
	}

	private Object createRequestedInstance(final HttpServletRequest req, final HttpServletResponse resp,
			final Parser bestFitParser) throws IOException, InstantiationException, IllegalAccessException {
		Object instance = null;
		if (bestFitParser != null) {
			final String input = req.getReader().lines().collect(Collectors.joining());
			try {
				instance = bestFitParser.parse(input, type);
			} catch (final ParserException e) {
				resp.getWriter().write(e.getMessage());
				LOGGER.warn("Cannot parse user Input", e);
			}
		} else {
			instance = type.newInstance();
			for (final Field field : ReflectionUtil.getAllFields(type)) {
				final Object fieldValue = fromString(field.getType(), req.getParameter(field.getName()));
				if (fieldValue != null) {
					ReflectionUtil.executeSet(field, instance, fieldValue);
				}
				LOGGER.info(() -> field.getName() + ": " + req.getParameter(field.getName()));
			}
		}
		return instance;
	}

	private void doPostCreate(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		assembleRequestObject(req, resp, instance -> {
			final UUID newId = dao.insertShallow(instance);
			resp.addHeader("id", newId.toString());
			return "";
		});
		resp.setStatus(HttpServletResponse.SC_CREATED);
	}

	private void doPostEdit(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		assembleRequestObject(req, resp, instance -> {
			final int updatedElements = dao.update(instance);
			return "Success\n" + updatedElements + " Element(s) updated";
		});
	}

	private void doGetCreate(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		resp.setCharacterEncoding(UTF_8);
		try (final PrintWriter writer = resp.getWriter()) {
			final Renderer renderer = getRenderer(req.getContentType());
			resp.setContentType(renderer.getContentType());
			writer.write(renderer.forCreate(type));
		}
	}

	private void doGetEdit(final T object, final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException {
		assert (object != null);
		resp.setCharacterEncoding(UTF_8);
		try (final PrintWriter writer = resp.getWriter()) {
			final Renderer renderer = getRenderer(req.getContentType());
			resp.setContentType(renderer.getContentType());
			writer.write(renderer.forEdit(object));
		}
	}

	@Override
	public String toString() {
		return path.toString();
	}
}
