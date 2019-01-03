package de.minee.jpa;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.logging.Logger;

import de.minee.util.ReflectionUtil;

class PreparedUpdate<S> {

	private static final Logger logger = Logger.getLogger(PreparedUpdate.class.getName());

	private final List<Field> fieldList = new ArrayList<>();
	private final Connection connection;
	private final PreparedStatement preparedStatement;
	private final boolean deepUpdate;

	private final Map<Field, PreparedStatement> mappingInsert = new HashMap<>();
	private final Map<Field, PreparedStatement> mappingDelete = new HashMap<>();
	private final Map<Field, PreparedStatement> mappingSelect = new HashMap<>();

	public PreparedUpdate(final Class<S> clazz, final Connection connection, final boolean deepUpdate)
			throws SQLException {
		this.connection = connection;
		this.deepUpdate = deepUpdate;
		final StringBuilder query = new StringBuilder();

		query.append("UPDATE ");
		query.append(clazz.getSimpleName());
		query.append(" SET ");
		final Field[] fields = clazz.getDeclaredFields();
		final StringJoiner stringJoiner = new StringJoiner(",");
		for (final Field field : fields) {
			field.setAccessible(true);
			fieldList.add(field);
			if (List.class.isAssignableFrom(field.getType())) {
				final ParameterizedType mapToType = (ParameterizedType) field.getGenericType();
				final Class<?> type = (Class<?>) mapToType.getActualTypeArguments()[0];
				mappingInsert.put(field, connection.prepareStatement(
						"INSERT INTO Mapping_" + clazz.getSimpleName() + "_" + field.getName() + " VALUES (?,?)"));
				mappingDelete.put(field,
						connection
								.prepareStatement("DELETE FROM Mapping_" + clazz.getSimpleName() + "_" + field.getName()
										+ " WHERE " + clazz.getSimpleName() + "=? AND " + type.getSimpleName() + "=?"));
				mappingSelect.put(field, connection.prepareStatement("SELECT " + type.getSimpleName() + " FROM Mapping_"
						+ clazz.getSimpleName() + "_" + field.getName() + " WHERE " + clazz.getSimpleName() + "=?"));
				continue;
			}
			stringJoiner.add(field.getName() + "=?");
		}
		query.append(stringJoiner.toString());
		query.append(" WHERE id = ?");

		final String updateQuery = query.toString();
		logger.info(updateQuery);
		preparedStatement = connection.prepareStatement(updateQuery);
	}

	private int execute(final S objectToUpdate) throws SQLException {
		assert (objectToUpdate != null);
		int i = 1;
		UUID objectId = null;
		for (final Field field : fieldList) {
			Object fieldElementToUpdate = ReflectionUtil.executeGet(field, objectToUpdate);

			if (List.class.isAssignableFrom(field.getType())) {
				continue;
			}

			if ("id".equals(field.getName())) {
				if (fieldElementToUpdate == null) {
					objectId = UUID.randomUUID();
					fieldElementToUpdate = objectId;
				} else {
					objectId = (UUID) fieldElementToUpdate;
				}
			}
			final Object dbObject = MappingHelper.getDbObject(fieldElementToUpdate);
			preparedStatement.setObject(i++, dbObject);
			if (deepUpdate && dbObject != fieldElementToUpdate && UUID.class.isInstance(dbObject)) {
				update(fieldElementToUpdate, connection, deepUpdate);
			}
		}

		for (final Field field : fieldList) {
			if (List.class.isAssignableFrom(field.getType())) {
				final Object fieldElementToUpdate = ReflectionUtil.executeGet(field, objectToUpdate);
				handleList(field, fieldElementToUpdate, objectId);
			}
		}
		// this is for 'UPDATE (...) WHERE id = ?'
		preparedStatement.setObject(i, objectId);
		return preparedStatement.executeUpdate();
	}

	private void handleList(final Field field, final Object fieldElementToUpdate, final UUID objectId)
			throws SQLException {
		if (fieldElementToUpdate == null) {
			return;
		}
		final PreparedStatement selectStatement = mappingSelect.get(field);
		final PreparedStatement deleteStatement = mappingDelete.get(field);
		final PreparedStatement insertStatement = mappingInsert.get(field);
		final Set<Object> existingElements = new HashSet<>();
		selectStatement.setObject(1, objectId);
		logger.info(selectStatement::toString);
		try (ResultSet rs = selectStatement.executeQuery()) {
			while (rs.next()) {
				existingElements.add(rs.getObject(1));
			}
		}

		final List<?> list = (List<?>) fieldElementToUpdate;
		for (final Object listElement : list) {
			if (listElement == null) {
				continue;
			}
			final Object element;
			if (MappingHelper.isSupportedType(listElement.getClass())) {
				element = listElement;
			} else {
				if (deepUpdate) {
					update(listElement, connection, deepUpdate);
				}
				element = MappingHelper.getId(listElement);
			}
			if (existingElements.contains(element)) {
				existingElements.remove(element);
			} else {
				insertStatement.setObject(1, objectId);
				insertStatement.setObject(2, element);
				logger.info(insertStatement::toString);
				insertStatement.execute();
			}
		}
		for (final Object element : existingElements) {
			deleteStatement.setObject(1, objectId);
			deleteStatement.setObject(2, element);
			logger.info(deleteStatement::toString);
			deleteStatement.execute();
		}
	}

	protected static <T> int update(final T objectToUpdate, final Connection connection, final boolean deepUpdate)
			throws SQLException {
		@SuppressWarnings("unchecked")
		final Class<T> clazz = (Class<T>) objectToUpdate.getClass();
		final PreparedUpdate<T> update = new PreparedUpdate<>(clazz, connection, deepUpdate);
		return update.execute(objectToUpdate);
	}
}
