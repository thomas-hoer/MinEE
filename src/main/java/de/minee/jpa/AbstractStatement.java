package de.minee.jpa;

import de.minee.util.Assertions;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Base class for the query builder that contain WHERE restrictions.
 *
 * @param <T> Class that corresponds to a database table
 */
public abstract class AbstractStatement<T> extends AbstractQuery implements IStatement<T> {

	private static final String INSTANTIATION_ERROR_MESSAGE = "Cannot instanciate object of type ";

	private static final Logger LOGGER = Logger.getLogger(AbstractStatement.class.getName());

	private final Class<T> clazz;
	private final Connection connection;
	private final List<AbstractAndOrConnection<T, ? extends IStatement<T>>> connections = new ArrayList<>();
	private final List<Field> fieldList = new ArrayList<>();
	private String additionalWhereClause;

	/**
	 * Creates a base for a SELECT statement.
	 *
	 * @param cls        Class that corresponds to a database table
	 * @param connection Database connection
	 */
	public AbstractStatement(final Class<T> cls, final Connection connection) {
		Assertions.assertNotNull(cls);
		Assertions.assertNotNull(connection);
		this.clazz = cls;
		this.connection = connection;
		for (final Field field : ReflectionUtil.getAllFields(cls)) {
			fieldList.add(field);
		}
	}

	@Override
	public <U extends IStatement<T>> void add(final AbstractAndOrConnection<T, U> connection) {
		connections.add(connection);
	}

	public AbstractAndOrConnection<T, AbstractStatement<T>> and() {
		return new AndQueryConnection<>(this);
	}

	public AbstractAndOrConnection<T, AbstractStatement<T>> or() {
		return new OrQueryConnection<>(this);
	}

	/**
	 * Selects an Object directly by its Id.
	 *
	 * @param id Id of the entry
	 * @return Object with Id id or null if no entry can be found
	 * @throws SQLException SQLException in case of an error
	 */
	@Override
	public T byId(final UUID id) throws SQLException {
		return byId(id, new HashMap<>());
	}

	/**
	 * Selects an Object directly by its Id or returns a cached Object of the
	 * current session. The cache improves performance, ensures object references
	 * and avoids endless cycles.
	 *
	 * @param id             Id of the entry
	 * @param handledObjects Object cache of fetched entries in the same session
	 * @return Object with Id id or null if no entry can be found
	 * @throws SQLException SQLException in case of an error
	 */
	protected T byId(final UUID id, final Map<Object, Object> handledObjects) throws SQLException {
		if (handledObjects.containsKey(id)) {
			return (T) handledObjects.get(id);
		}
		final StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ");
		query.append(clazz.getSimpleName());
		query.append(" WHERE id = '");
		query.append(id.toString());
		query.append("'");
		final String selectQuery = query.toString();
		LOGGER.info(selectQuery);

		try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(selectQuery)) {
			final T obj = clazz.newInstance();
			handledObjects.put(id, obj);
			return rs.next() ? mapResultSet(rs, obj, handledObjects) : null;
		} catch (final InstantiationException | IllegalAccessException e) {
			throw new SQLException(INSTANTIATION_ERROR_MESSAGE + clazz.getName(), e);
		}
	}

	/**
	 * Executes a prepared statement by handing over the query arguments.
	 *
	 * @param args Arguments for the prepared statement
	 * @return A list of the found database entries
	 * @throws SQLException SQLException in case of an error
	 */
	public List<T> execute(final Collection<?> args) throws SQLException {
		final String query = assembleQuery();
		LOGGER.info(query);

		final List<T> resultList = new ArrayList<>();
		ResultSet resultSet = null;
		try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
			int i = 1;
			final Map<Object, Object> handledObjects = new HashMap<>();
			for (final Object arg : args) {
				preparedStatement.setObject(i++, arg);
			}
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				final T obj = clazz.newInstance();
				resultList.add(mapResultSet(resultSet, obj, handledObjects));
			}
		} catch (final InstantiationException | IllegalAccessException e) {
			throw new SQLException(INSTANTIATION_ERROR_MESSAGE + clazz.getName(), e);
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
		}
		return resultList;
	}

	/**
	 * Executes the Query.
	 *
	 * @return A list of the found database entries
	 * @throws SQLException SQLException in case of an error
	 */
	@Override
	public List<T> execute() throws SQLException {
		final String query = assembleQuery();
		LOGGER.info(query);
		final List<T> resultList = new ArrayList<>();
		try (final Statement statement = connection.createStatement();
				final ResultSet rs = statement.executeQuery(query)) {
			final Map<Object, Object> handledObjects = new HashMap<>();
			while (rs.next()) {
				final T obj = clazz.newInstance();
				resultList.add(mapResultSet(rs, obj, handledObjects));
			}
		} catch (final InstantiationException | IllegalAccessException e) {
			throw new SQLException(INSTANTIATION_ERROR_MESSAGE + clazz.getName(), e);
		}
		return resultList;
	}

	private String assembleQuery() {
		final StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ");
		query.append(clazz.getSimpleName());
		query.append(" ");
		for (final AbstractAndOrConnection<T, ?> queryConnection : connections) {
			final WhereClause<?, ?, ?> whereClause = queryConnection.getClause();
			query.append(whereClause.getJoinClause());
		}
		if (!connections.isEmpty() || (additionalWhereClause != null)) {
			query.append("WHERE ");
		}
		if (additionalWhereClause != null) {
			query.append(additionalWhereClause);
		}
		//
		for (final AbstractAndOrConnection<T, ?> queryConnection : connections) {
			final WhereClause<?, ?, ?> whereClause = queryConnection.getClause();
			query.append(queryConnection.getConnectionString());
			query.append(whereClause.toString());
		}
		return query.toString();
	}

	private T mapResultSet(final ResultSet rs, final T instance, final Map<Object, Object> handledObjects)
			throws SQLException {
		for (final Field field : fieldList) {
			try {
				if (List.class.isAssignableFrom(field.getType())) {
					handleList(instance, field, handledObjects);
					continue;
				}
				handleFieldColumn(field, rs, instance, handledObjects);
			} catch (final IllegalAccessException e) {
				throw new SQLException("Unable to set value for Field " + field.getName() + " in " + clazz.getName(),
						e);
			}
		}
		return instance;
	}

	abstract void handleList(final T obj, final Field field, final Map<Object, Object> handledObjects)
			throws SQLException, IllegalAccessException;

	abstract void handleFieldColumn(final Field field, final ResultSet rs, final T obj,
			final Map<Object, Object> handledObjects) throws SQLException, IllegalAccessException;

	@Override
	protected Connection getConnection() {
		return connection;
	}

	@Override
	public Class<T> getType() {
		return clazz;
	}

	@Override
	public AbstractStatement<T> query(final String whereClause) {
		Assertions.assertNotEmpty(whereClause);
		additionalWhereClause = whereClause;
		return this;
	}

}
