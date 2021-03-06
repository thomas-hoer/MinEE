package de.minee.jpa;

import de.minee.util.Assertions;
import de.minee.util.ReflectionUtil;
import de.minee.util.logging.Logger;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

class PreparedUpdate<S> extends AbstractPreparedQuery<S> {

	private static final Logger LOGGER = Logger.getLogger(PreparedUpdate.class);

	private static final String UPDATE_TEMPLATE = "UPDATE %s SET %s WHERE id = ?";
	private final List<Field> fieldList = new ArrayList<>();
	private final PreparedStatement preparedStatement;

	/**
	 * Creates a PreparedStatement for a update query.
	 *
	 * @param cls        Class corresponding to the table where a entry should be
	 *                   updated
	 * @param connection Database connection
	 * @param cascade    Rule how referenced objects should be threaded @
	 *                   SQLException in case of an error
	 */
	public PreparedUpdate(final Class<S> cls, final Connection connection, final Cascade cascade) {
		super(connection, cascade);
		final StringJoiner stringJoiner = new StringJoiner(",");
		for (final Field field : ReflectionUtil.getAllFields(cls)) {
			fieldList.add(field);
			if (List.class.isAssignableFrom(field.getType())) {
				prepareInsert(field);
				prepareDelete(field);
				prepareSelect(field);
				continue;
			}
			stringJoiner.add(field.getName() + "=?");
		}

		final String updateQuery = String.format(UPDATE_TEMPLATE, cls.getSimpleName(), stringJoiner);
		LOGGER.info(updateQuery);
		preparedStatement = prepare(updateQuery);
	}

	private int execute(final S objectToUpdate) throws SQLException {
		Assertions.assertNotNull(objectToUpdate, "Instance for update should not be null");
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
				update(fieldElementToUpdate, getConnection(), cascade);
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
		LOGGER.info(preparedStatement::toString);
		return preparedStatement.executeUpdate();
	}

	private void handleList(final Field field, final Object fieldElementToUpdate, final UUID objectId)
			throws SQLException {
		if (fieldElementToUpdate == null) {
			return;
		}
		final PreparedStatement selectStatement = mappingSelect.get(field);
		final PreparedStatement insertStatement = mappingInsert.get(field);
		final Set<Object> existingElements = new HashSet<>();
		selectStatement.setObject(1, objectId);
		LOGGER.info(selectStatement::toString);
		executeQuery(selectStatement, rs -> existingElements.add(rs.getObject(1)));

		final List<?> list = (List<?>) fieldElementToUpdate;
		for (final Object listElement : list) {
			if (listElement == null) {
				continue;
			}
			final Object element;
			if (MappingHelper.isSupportedType(listElement.getClass())) {
				element = listElement;
			} else if (listElement.getClass().isEnum()) {
				element = listElement.toString();
			} else {
				if (Cascade.UPDATE == cascade) {
					update(listElement, getConnection(), cascade);
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
		removeReferences(field, objectId, existingElements);
	}

	protected static <T> int update(final T objectToUpdate, final Connection connection, final Cascade cascade) {
		@SuppressWarnings("unchecked")
		final Class<T> clazz = (Class<T>) objectToUpdate.getClass();
		final PreparedUpdate<T> update = new PreparedUpdate<>(clazz, connection, cascade);
		try {
			return update.execute(objectToUpdate);
		} catch (final SQLException e) {
			throw new DatabaseException(e);
		}

	}
}
