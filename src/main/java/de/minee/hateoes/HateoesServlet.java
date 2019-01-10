package de.minee.hateoes;

import de.minee.cdi.CdiAwareHttpServlet;
import de.minee.cdi.CdiUtil;
import de.minee.hateoes.renderer.AbstractRenderer;
import de.minee.hateoes.renderer.JsonRenderer;
import de.minee.jpa.AbstractDAO;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HateoesServlet extends CdiAwareHttpServlet {

	private static final long serialVersionUID = -5213801706760630081L;
	private static final Logger LOGGER = Logger.getLogger(HateoesServlet.class.getName());

	private final List<ManagedResource<?>> managedResources = new ArrayList<>();

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

		for (final Field field : cls.getDeclaredFields()) {
			final ManagedResource<?> managedResource = checkHateoesResourceAnnotation(field);
			daoNeeded |= checkPersistentAnnotation(needsPersistentDao, inMemTypes, field, managedResource);
			persistentDao = checkDataAccessObjectAnnotation(persistentDao, field);
		}

		if (daoNeeded && persistentDao == null) {
			throw new HateoesException("A DataAccessObject is needed to provide a persistend managed HateoesResouce");
		}

		final AbstractDAO inMemDao = new AbstractDAO() {
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
		};

		for (final ManagedResource<?> managedResource : managedResources) {
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
			managedResource = new ManagedResource<>(annotation.value(), annotation.allowedOperations(), resourceType);
			managedResources.add(managedResource);
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
			field.setAccessible(true);
			LOGGER.info("Use as DataAccessObject: " + field.getName());
			try {
				final AbstractDAO newDAOInstance = (AbstractDAO) CdiUtil.getInstance(field.getType());
				field.set(this, newDAOInstance);
				return newDAOInstance;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new HateoesException(e);
			}
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
		for (final ManagedResource<?> managedResource : managedResources) {
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
			final Object[] availableResources = managedResources.stream().map(ManagedResource::toString).toArray();
			writer.write(renderer.render(availableResources));
		}
	}

}
