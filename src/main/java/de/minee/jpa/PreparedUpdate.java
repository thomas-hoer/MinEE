package de.minee.jpa;

import de.minee.util.Assertions;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.logging.Logger;

class PreparedUpdate<S> extends PreparedQueryBase<S> {

	private static final Logger LOGGER = Logger.getLogger(PreparedUpdate.class.getName());

	private final List<Field> fieldList = new ArrayList<>();
	private final PreparedStatement preparedStatement;

	public PreparedUpdate(final Class<S> clazz, final Connection connection, final Cascade cascade)
			throws SQLException {
		super(connection, cascade);
		final StringBuilder query = new StringBuilder();

		query.append("UPDATE ");
		query.append(clazz.getSimpleName());
		query.append(" SET ");
		final Field[] fields = clazz.getDeclaredFields();
		final StringJoiner stringJoiner = new StringJoiner(",");
		for (final Field field : fields) {
			fieldList.add(field);
			if (List.class.isAssignableFrom(field.getType())) {
				prepareInsert(connection, field);
				prepareDelete(connection, field);
				prepareSelect(connection, field);
				continue;
			}
			stringJoiner.add(field.getName() + "=?");
		}
		query.append(stringJoiner.toString());
		query.append(" WHERE id = ?");

		final String updateQuery = query.toString();
		LOGGER.info(updateQuery);
		preparedStatement = connection.prepareStatement(updateQuery);
	}

	private int execute(final S objectToUpdate) throws SQLException {
		Assertions.assertNotNull(objectToUpdate);
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
			if (Cascade.UPDATE == cascade && dbObject != fieldElementToUpdate && UUID.class.isInstance(dbObject)) {
				update(fieldElementToUpdate, connection, cascade);
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
		LOGGER.info(selectStatement::toString);
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
				if (Cascade.UPDATE == cascade) {
					update(listElement, connection, cascade);
				}
				element = MappingHelper.getId(listElement);
			}
			if (existingElements.contains(element)) {
				existingElements.remove(element);
			} else {
				insertStatement.setObject(1, objectId);
				insertStatement.setObject(2, element);
				LOGGER.info(insertStatement::toString);
				insertStatement.execute();
			}
		}
		for (final Object element : existingElements) {
			deleteStatement.setObject(1, objectId);
			deleteStatement.setObject(2, element);
			LOGGER.info(deleteStatement::toString);
			deleteStatement.execute();
		}
	}

	protected static <T> int update(final T objectToUpdate, final Connection connection, final Cascade cascade)
			throws SQLException {
		@SuppressWarnings("unchecked")
		final Class<T> clazz = (Class<T>) objectToUpdate.getClass();
		final PreparedUpdate<T> update = new PreparedUpdate<>(clazz, connection, cascade);
		return update.execute(objectToUpdate);
	}
}
