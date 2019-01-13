package de.minee.hateoes;

import de.minee.hateoes.renderer.AbstractRenderer;
import de.minee.hateoes.renderer.HtmlRenderer;
import de.minee.jpa.AbstractDAO;
import de.minee.util.ReflectionUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class ManagedResource<T> {

	private static final Logger LOGGER = Logger.getLogger(ManagedResource.class.getName());

	private final String[] path;
	private final boolean[] varMap;
	private final Set<Operation> allowedOperations;
	private final Class<T> type;
	private AbstractDAO dao;
	private final AbstractRenderer renderer = new HtmlRenderer();

	ManagedResource(final String path, final Operation[] allowedOperations, final Class<T> type) {
		this.path = path.split("/");
		this.varMap = new boolean[this.path.length];
		final List<String> el = new ArrayList<>();
		for (int i = 0; i < this.path.length; i++) {
			final String pathPart = this.path[i];
			if (pathPart.startsWith("{") && pathPart.endsWith("}")) {
				this.path[i] = pathPart.substring(1, pathPart.length() - 1);
				varMap[i] = true;
				el.add(pathPart);
			} else {
				varMap[i] = false;
			}
		}
		this.allowedOperations = new HashSet<>(Arrays.asList(allowedOperations));
		if (this.allowedOperations.contains(Operation.ALL)) {
			this.allowedOperations.add(Operation.GET);
			this.allowedOperations.add(Operation.POST);
			this.allowedOperations.add(Operation.PUT);
			this.allowedOperations.add(Operation.DELETE);
		}
		this.type = type;

	}

	public void setDao(final AbstractDAO dao) {
		this.dao = dao;
	}

	boolean isMatch(final String pathInfo) {
		final String[] pathSplit = pathInfo.split("/");
		boolean isMatch = false;
		if (pathSplit.length == path.length || pathSplit.length == path.length + 1) {
			for (int i = 0; i < path.length; i++) {
				if (!varMap[i] && !path[i].equals(pathSplit[i])) {
					return false;
				}
			}
			if (pathSplit.length == path.length + 1) {
				final String action = pathSplit[path.length];
				isMatch = "create".equals(action) || "edit".equals(action);
			} else {
				isMatch = true;
			}
		}
		return isMatch;
	}

	boolean isMethodAllowed(final String method) {
		return allowedOperations.contains(Operation.valueOf(method));
	}

	void serve(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		final String pathInfo = req.getPathInfo();
		final String[] pathSplit = pathInfo.split("/");
		final List<?> result;
		try {
			if (pathSplit.length == path.length + 1) {
				final String action = pathSplit[path.length];
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
		} catch (final SQLException e) {
			LOGGER.log(Level.SEVERE, "Cannot access database or database entities", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		try (final PrintWriter writer = resp.getWriter()) {
			if (result.isEmpty()) {
				writer.write(renderer.render(null));
			} else if (result.size() == 1) {
				writer.write(renderer.render(result.get(0)));
			} else {
				writer.write(renderer.render(result));
			}
		}
	}

	private void serveEdit(final HttpServletRequest req, final HttpServletResponse resp, final String[] pathSplit)
			throws SQLException, IOException {
		final String method = req.getMethod();
		final List<T> result = getSelectedResource(pathSplit);
		if (result == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else if (result.size() != 1) {
			resp.sendError(HttpServletResponse.SC_CONFLICT);
		} else {
			if (method.equals(Operation.GET.name())) {
				doGetEdit(result.get(0), resp);
			} else if (method.equals(Operation.POST.name())) {
				doPostEdit(req, resp);
			}
		}
	}

	private void serveCreate(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		final String method = req.getMethod();
		if (method.equals(Operation.GET.name())) {
			doGetCreate(resp);
		} else if (method.equals(Operation.POST.name())) {
			doPostCreate(req, resp);
		}
	}

	private List<T> getSelectedResource(final String[] pathSplit) throws SQLException {
		final List<String> parameter = new ArrayList<>();
		final StringJoiner stringJoiner = new StringJoiner(" AND ");
		for (int i = 0; i < path.length; i++) {
			if (varMap[i]) {
				stringJoiner.add(path[i] + "=?");
				parameter.add(pathSplit[i]);
			}
		}
		if (parameter.isEmpty()) {
			return dao.select(type).execute();
		}
		return dao.select(type).query(stringJoiner.toString()).execute(parameter);
	}

	private Object fromString(final Class<?> type, final String parameter) {
		if (parameter == null) {
			return null;
		}
		if (int.class.equals(type) || Integer.class.equals(type)) {
			return Integer.valueOf(parameter);
		}
		if (String.class.equals(type)) {
			return parameter;
		}
		if (UUID.class.equals(type)) {
			return "".equals(parameter) ? null : UUID.fromString(parameter);
		}

		final Field refId = ReflectionUtil.getDeclaredField(type, "id");
		if (refId != null) {
			if ("".equals(parameter)) {
				return null;
			}
			final UUID uuid = UUID.fromString(parameter);
			try {
				final Object result = dao.select(type).byId(uuid);
				if (result == null) {
					throw new HateoesException(type.getSimpleName() + " with Id " + parameter + " not found");
				}
				return result;
			} catch (final SQLException e) {
				throw new HateoesException("Cannot translate String to type " + type.getSimpleName(), e);
			}
		}
		throw new HateoesException("Not able to map String to type " + type.getSimpleName());
	}

	private interface ManagedResourceConsumer {
		String accept(Object instance) throws SQLException;
	}

	private void assembleRequestObject(final HttpServletRequest req, final HttpServletResponse resp,
			final ManagedResourceConsumer consumer) throws IOException {
		try {
			final Object instance = type.newInstance();
			for (final Field field : ReflectionUtil.getAllFields(type)) {
				final Object fieldValue = fromString(field.getType(), req.getParameter(field.getName()));
				if (fieldValue != null) {
					ReflectionUtil.executeSet(field, instance, fieldValue);
				}
				LOGGER.info(() -> field.getName() + ": " + req.getParameter(field.getName()));
			}
			resp.getWriter().write(consumer.accept(instance));
		} catch (final RuntimeException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			throw e;
		} catch (InstantiationException | IllegalAccessException | SQLException | IOException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new IOException(e);
		}
	}

	private void doPostCreate(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		assembleRequestObject(req, resp, instance -> {
			final UUID newId = dao.insertShallow(instance);
			return "Success\nNew ID:" + newId;
		});
	}

	private void doPostEdit(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		assembleRequestObject(req, resp, instance -> {
			final int updatedElements = dao.update(instance);
			return "Success\n" + updatedElements + " Elements updated";
		});
	}

	private void doGetCreate(final HttpServletResponse resp) throws IOException {
		try (final PrintWriter writer = resp.getWriter()) {
			writer.write(renderer.forCreate(type));
		}
	}

	private void doGetEdit(final T object, final HttpServletResponse resp) throws IOException {
		assert (object != null);
		try (final PrintWriter writer = resp.getWriter()) {
			writer.write(renderer.forEdit(object));
		}
	}

	@Override
	public String toString() {
		final StringJoiner joiner = new StringJoiner("/");
		Arrays.stream(path).forEach(joiner::add);
		return joiner.toString();
	}
}
