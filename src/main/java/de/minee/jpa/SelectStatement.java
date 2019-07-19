package de.minee.jpa;

import de.minee.util.Logger;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SelectStatement<T> extends AbstractStatement<T> {

	private static final Logger LOGGER = Logger.getLogger(SelectStatement.class);
	private static final String INSTANTIATION_ERROR_MESSAGE = "Cannot instanciate object of type ";

	private final List<Field> fieldList = new ArrayList<>();

	/**
	 * Creates a new Select Statement for table cls
	 *
	 * @param cls        Class representing a database table
	 * @param connection Database connection
	 */
	public SelectStatement(final Class<T> cls, final Connection connection) {
		super(cls, connection);
		for (final Field field : ReflectionUtil.getAllFields(cls)) {
			fieldList.add(field);
		}
	}

	protected static <S> AbstractStatement<S> select(final Class<S> clazz, final Connection connection) {
		return new SelectStatement<>(clazz, connection);
	}

	/**
	 * Executes a prepared statement by handing over the query arguments.
	 *
	 * @param args Arguments for the prepared statement
	 * @return A list of the found database entries @ SQLException in case of an
	 *         error
	 */
	@Override
	public List<T> execute(final Collection<?> args) {
		final String query = assembleFullSelectQuery();
		LOGGER.info(query);

		final List<T> resultList = new ArrayList<>();
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(query)) {
			int i = 1;
			final Map<Object, Object> handledObjects = new HashMap<>();
			for (final Object arg : args) {
				preparedStatement.setObject(i++, arg);
			}
			LOGGER.info(preparedStatement::toString);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					final T obj = getType().newInstance();
					resultList.add(mapResultSet(resultSet, obj, handledObjects));
				}
			}
		} catch (final SQLException e) {
			throw new DatabaseException(e);
		} catch (final InstantiationException | IllegalAccessException e) {
			throw new DatabaseException(INSTANTIATION_ERROR_MESSAGE + getType().getName(), e);
		}
		return resultList;
	}

	/**
	 * Executes the Query.
	 *
	 * @return A list of the found database entries @ SQLException in case of an
	 *         error
	 */
	@Override
	public List<T> execute() {
		final String query = assembleFullSelectQuery();
		LOGGER.info(query);
		final List<T> resultList = new ArrayList<>();
		try (final Statement statement = getConnection().createStatement();
				final ResultSet rs = statement.executeQuery(query)) {
			final Map<Object, Object> handledObjects = new HashMap<>();
			while (rs.next()) {
				final T obj = getType().newInstance();
				resultList.add(mapResultSet(rs, obj, handledObjects));
			}
		} catch (final SQLException e) {
			throw new DatabaseException(e);
		} catch (final InstantiationException | IllegalAccessException e) {
			throw new DatabaseException(INSTANTIATION_ERROR_MESSAGE + getType().getName(), e);
		}
		return resultList;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected T byId(final UUID id, final Map<Object, Object> handledObjects) {
		if (handledObjects.containsKey(id)) {
			return (T) handledObjects.get(id);
		}
		final StringBuilder query = new StringBuilder();
		query.append("SELECT " + getType().getSimpleName() + ".* FROM ");
		query.append(getType().getSimpleName());
		query.append(" WHERE id = '");
		query.append(id.toString());
		query.append("'");
		final String selectQuery = query.toString();
		LOGGER.info(selectQuery);

		try (Statement statement = getConnection().createStatement();
				ResultSet rs = statement.executeQuery(selectQuery)) {
			final T obj = getType().newInstance();
			handledObjects.put(id, obj);
			return rs.next() ? mapResultSet(rs, obj, handledObjects) : null;
		} catch (final SQLException e) {
			throw new DatabaseException(e);
		} catch (final InstantiationException | IllegalAccessException e) {
			throw new DatabaseException(INSTANTIATION_ERROR_MESSAGE + getType().getName(), e);
		}
	}

	private T mapResultSet(final ResultSet rs, final T instance, final Map<Object, Object> handledObjects) {
		for (final Field field : fieldList) {
			if (List.class.isAssignableFrom(field.getType())) {
				handleList(instance, field, handledObjects);
				continue;
			}
			handleFieldColumn(field, rs, instance, handledObjects);
		}
		return instance;
	}

	void handleList(final T obj, final Field field, final Map<Object, Object> handledObjects) {
		final ParameterizedType mapToType = (ParameterizedType) field.getGenericType();
		final Class<?> type = (Class<?>) mapToType.getActualTypeArguments()[0];
		final boolean supportedType = MappingHelper.isSupportedType(type);
		final boolean isEnum = type.isEnum();
		final List<Object> list = new ArrayList<>();
		final String query = "SELECT " + type.getSimpleName() + " FROM Mapping_" + getType().getSimpleName() + "_"
				+ field.getName() + " WHERE " + getType().getSimpleName() + " = '" + MappingHelper.getId(obj) + "'";
		executeQuery(query, resultSet -> {
			try {
				if (supportedType) {
					list.add(resultSet.getObject(1));
				} else if(isEnum) {
					list.add(Enum.valueOf((Class<? extends Enum>)type, resultSet.getString(1)));
				} else {
					final Object o = select(type, getConnection()).byId((UUID) resultSet.getObject(1), handledObjects);
					list.add(o);
				}
			} catch (final SQLException e) {
				throw new DatabaseException(e);
			}
		});
		ReflectionUtil.executeSet(field, obj, list);
	}

	void handleFieldColumn(final Field field, final ResultSet rs, final T obj,
			final Map<Object, Object> handledObjects) {
		try {
			final Object value = rs.getObject(field.getName());
			if (value != null) {
				if (field.getType().isAssignableFrom(value.getClass())) {
					ReflectionUtil.executeSet(field, obj, value);
				} else if (UUID.class.equals(value.getClass())) {
					final Object resolvedValue = select(field.getType(), getConnection()).byId((UUID) value,
							handledObjects);
					ReflectionUtil.executeSet(field, obj, resolvedValue);
				} else if (field.getType().isEnum()) {
					final String stringValue = rs.getString(field.getName());
					ReflectionUtil.executeSet(field, obj, MappingHelper.getEnum(field.getType(), stringValue));
				} else if (field.getType().isArray()) {
					if (byte.class.equals(field.getType().getComponentType())) {
						ReflectionUtil.executeSet(field, obj, Base64.getDecoder().decode((String) value));
					} else {
						ReflectionUtil.executeSet(field, obj, rs.getArray(field.getName()).getArray());
					}
				} else {
					throw new DatabaseException("Cannot set value of type " + value.getClass() + " to "
							+ field.getName() + " of type " + field.getType());
				}
			}
		} catch (final SQLException e) {
			throw new DatabaseException(e);
		}
	}

}
