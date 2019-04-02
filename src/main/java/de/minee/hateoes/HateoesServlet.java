package de.minee.hateoes;

import de.minee.cdi.CdiAwareHttpServlet;
import de.minee.cdi.CdiUtil;
import de.minee.hateoes.renderer.AbstractRenderer;
import de.minee.hateoes.renderer.HtmlRenderer;
import de.minee.hateoes.renderer.JsonRenderer;
import de.minee.jpa.AbstractDAO;
import de.minee.util.Logger;
import de.minee.util.ReflectionUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HateoesServlet extends CdiAwareHttpServlet {

	private static final long serialVersionUID = -5213801706760630081L;
	private static final Logger LOGGER = Logger.getLogger(HateoesServlet.class);

	private final HateoesContext context = new HateoesContext();

	@Override
	public void init() throws ServletException {
		super.init();
		initHateoesResources();
	}

	private void initHateoesResources() {
		final Class<? extends HateoesServlet> cls = this.getClass();
		boolean daoNeeded = false;
		AbstractDAO persistentDao = null;
		final Set<ManagedResource<?>> needsPersistentDao = new HashSet<>();
		final Set<Class<?>> inMemTypes = new HashSet<>();

		for (final Field field : ReflectionUtil.getAllFields(cls)) {
			context.addType(field.getType());
		}
		for (final Field field : ReflectionUtil.getAllFields(cls)) {
			final ManagedResource<?> managedResource = checkHateoesResourceAnnotation(field);
			daoNeeded |= checkPersistentAnnotation(needsPersistentDao, inMemTypes, field, managedResource);
			persistentDao = checkDataAccessObjectAnnotation(persistentDao, field);
		}

		if (daoNeeded && persistentDao == null) {
			throw new HateoesException("A DataAccessObject is needed to provide a persistend managed HateoesResouce");
		}

		final AbstractDAO inMemDao = new InMemDAO(inMemTypes);
		final AbstractRenderer renderer = new HtmlRenderer();

		for (final ManagedResource<?> managedResource : context.getManagedResources()) {
			managedResource.setRenderer(renderer);
			if (needsPersistentDao.contains(managedResource)) {
				managedResource.setDao(persistentDao);
			} else {
				managedResource.setDao(inMemDao);
			}
		}

	}

	private ManagedResource<?> checkHateoesResourceAnnotation(final Field field) {
		ManagedResource<?> managedResource = null;
		final HateoesResource annotation = field.getAnnotation(HateoesResource.class);
		if (annotation != null) {
			final Class<?> resourceType = field.getType();
			managedResource = new ManagedResource<>(context, annotation.value(), annotation.allowedOperations(),
					resourceType);
			context.addResource(managedResource);
		}
		return managedResource;
	}

	private static boolean checkPersistentAnnotation(final Set<ManagedResource<?>> needsPersistentDao,
			final Set<Class<?>> inMemTypes, final Field field, final ManagedResource<?> managedResource) {
		boolean daoNeeded = false;
		if (field.getAnnotation(Persistent.class) != null) {
			if (managedResource == null) {
				throw new HateoesException(
						"You have to also declare @HateoesResouce for @Persistent on field " + field.getName());
			}
			daoNeeded = true;
			needsPersistentDao.add(managedResource);
		} else if (managedResource != null) {
			inMemTypes.add(field.getType());
		}
		return daoNeeded;
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
		for (final ManagedResource<?> managedResource : context.getManagedResources()) {
			if (managedResource.isMatch(pathInfo) && managedResource.isMethodAllowed(method)) {
				managedResource.serve(req, resp);
				return;
			}
		}
		resp.sendError(HttpServletResponse.SC_NOT_FOUND);

	}

	private void handleRoot(final HttpServletResponse resp) throws IOException {
		final AbstractRenderer renderer = new JsonRenderer();
		try (final PrintWriter writer = resp.getWriter()) {
			final Object[] availableResources = context.getManagedResources().stream().map(ManagedResource::toString)
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
		protected int updateDatabaseSchema(final int oldDbSchemaVersion) throws SQLException {
			for (final Class<?> type : inMemTypes) {
				createTable(type);
			}
			return 1;
		}
	}

	public static class HateoesContext {

		private final List<ManagedResource<?>> managedResources = new ArrayList<>();
		private final Map<String, Class<?>> knownTypes = new HashMap<>();

		public Class<?> getTypeByName(final String typeName) {
			return knownTypes.get(typeName);
		}

		private void addResource(final ManagedResource<?> managedResource) {
			managedResources.add(managedResource);
		}

		private List<ManagedResource<?>> getManagedResources() {
			return managedResources;
		}

		private void addType(final Class<?> type) {
			knownTypes.put(type.getSimpleName(), type);
		}

	}
}
