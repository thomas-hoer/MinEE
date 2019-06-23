package de.minee.rest;

import de.minee.cdi.CdiAwareHttpServlet;
import de.minee.cdi.CdiUtil;
import de.minee.jpa.AbstractDAO;
import de.minee.rest.renderer.JsonRenderer;
import de.minee.rest.renderer.Renderer;
import de.minee.util.Logger;
import de.minee.util.ReflectionUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestServlet extends CdiAwareHttpServlet {

	private static final long serialVersionUID = -5213801706760630081L;
	private static final Logger LOGGER = Logger.getLogger(RestServlet.class);

	private final HateoesContext context = new HateoesContext();

	@Override
	public void init() throws ServletException {
		super.init();
		initManagedResources();
		initMethodResources();
	}

	private void initMethodResources() {
		final Class<? extends RestServlet> cls = this.getClass();
		for (final Method method : cls.getMethods()) {
			final RestResource annotation = method.getAnnotation(RestResource.class);
			if (annotation != null) {
				final MethodResource resource = new MethodResource(annotation.value(), annotation.allowedOperations(),
						this, method);
				Arrays.stream(annotation.consumes()).map(CdiUtil::getInstance).forEach(resource::addParser);
				Arrays.stream(annotation.produces()).map(CdiUtil::getInstance).forEach(resource::addRenderer);
				context.addResource(resource);
			}
		}
	}

	private void initManagedResources() {
		final Class<? extends RestServlet> cls = this.getClass();
		boolean daoNeeded = false;
		AbstractDAO persistentDao = null;
		final Set<ManagedResource<?>> needsPersistentDao = new HashSet<>();
		final Set<ManagedResource<?>> needsInMemDao = new HashSet<>();
		final Set<Class<?>> inMemTypes = new HashSet<>();

		for (final Field field : ReflectionUtil.getAllFields(cls)) {
			context.addType(field.getType());
		}
		for (final Field field : ReflectionUtil.getAllFields(cls)) {
			final ManagedResource<?> managedResource = checkHateoesResourceAnnotation(field);
			if (managedResource != null) {
				context.addResource(managedResource);
				if (field.getAnnotation(Persistent.class) != null) {
					needsPersistentDao.add(managedResource);
					daoNeeded = true;
				} else {
					needsInMemDao.add(managedResource);
					inMemTypes.add(field.getType());
				}
			}
			persistentDao = checkDataAccessObjectAnnotation(persistentDao, field);
		}

		if (daoNeeded && persistentDao == null) {
			throw new RestException("A DataAccessObject is needed to provide a persistend managed HateoesResouce");
		}

		final AbstractDAO inMemDao = new InMemDAO(inMemTypes);
		final AbstractDAO dao = persistentDao;

		needsInMemDao.forEach(resource -> resource.setDao(inMemDao));
		needsPersistentDao.forEach(resource -> resource.setDao(dao));
		needsInMemDao.forEach(context::addResource);
		needsPersistentDao.forEach(context::addResource);
	}

	private ManagedResource<?> checkHateoesResourceAnnotation(final Field field) {
		ManagedResource<?> managedResource = null;
		final RestResource annotation = field.getAnnotation(RestResource.class);
		if (annotation != null) {
			final Class<?> resourceType = field.getType();
			managedResource = new ManagedResource<>(context, annotation.value(), annotation.allowedOperations(),
					resourceType);
			Arrays.stream(annotation.consumes()).map(CdiUtil::getInstance).forEach(managedResource::addParser);
			Arrays.stream(annotation.produces()).map(CdiUtil::getInstance).forEach(managedResource::addRenderer);
		}
		return managedResource;
	}

	private AbstractDAO checkDataAccessObjectAnnotation(final AbstractDAO persistentDAO, final Field field) {
		if (field.getAnnotation(DataAccessObject.class) != null
				&& AbstractDAO.class.isAssignableFrom(field.getType())) {
			LOGGER.info("Use as DataAccessObject: " + field.getName());
			final AbstractDAO newDAOInstance = (AbstractDAO) CdiUtil.getInstance(field.getType());
			ReflectionUtil.executeSet(field, this, newDAOInstance);
			return newDAOInstance;
		}
		return persistentDAO;
	}

	@Override
	protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		final String pathInfo = req.getPathInfo();
		if (pathInfo == null || "/".equals(pathInfo)) {
			handleRoot(resp);
			return;
		}
		final String method = req.getMethod();
		for (final AbstractResource resource : context.getResources()) {
			if (resource.isMatch(pathInfo) && resource.isMethodAllowed(method)) {
				resource.serve(req, resp);
				return;
			}
		}
		resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	private void handleRoot(final HttpServletResponse response) throws IOException {
		final Renderer renderer = new JsonRenderer();
		response.setContentType(renderer.getContentType());
		response.setCharacterEncoding("UTF-8");
		try (final PrintWriter writer = response.getWriter()) {
			final Object[] availableResources = context.getResources().stream().map(AbstractResource::toString)
					.toArray();
			writer.write(renderer.render(availableResources));
		}
	}

	private static final class InMemDAO extends AbstractDAO {
		private final Set<Class<?>> inMemTypes;

		private InMemDAO(final Set<Class<?>> inMemTypes) {
			this.inMemTypes = inMemTypes;
		}

		@Override
		protected String getConnectionString() {
			return "jdbc:h2:mem:";
		}

		@Override
		protected String getUserName() {
			return "";
		}

		@Override
		protected String getPassword() {
			return "";
		}

		@Override
		protected int updateDatabaseSchema(final int oldDbSchemaVersion) {
			for (final Class<?> type : inMemTypes) {
				createTable(type);
			}
			return 1;
		}
	}

	public static class HateoesContext {

		private final List<AbstractResource> resources = new ArrayList<>();
		private final Map<String, Class<?>> knownTypes = new HashMap<>();

		public Class<?> getTypeByName(final String typeName) {
			return knownTypes.get(typeName);
		}

		private void addResource(final AbstractResource resource) {
			resources.add(resource);
		}

		private List<AbstractResource> getResources() {
			return resources;
		}

		private void addType(final Class<?> type) {
			knownTypes.put(type.getSimpleName(), type);
		}

	}
}