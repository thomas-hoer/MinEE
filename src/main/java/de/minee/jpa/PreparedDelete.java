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
import java.util.UUID;

public class PreparedDelete<T> extends AbstractPreparedQuery<T> {

	private static final Logger LOGGER = Logger.getLogger(PreparedDelete.class);

	private static final String DELETE_TEMPLATE = "DELETE FROM %s WHERE id = ?";
	private final List<Field> fieldList = new ArrayList<>();
	private final PreparedStatement preparedStatement;

	/**
	 * Creates a PreparedStatement for a delete query.
	 *
	 * @param clazz      Class corresponding to the table where a entry should be
	 *                   deleted
	 * @param connection Database connection
	 * @param cascade    Rule how referenced objects should be threaded @
	 *                   SQLException in case of an error
	 */
	public PreparedDelete(final Class<T> clazz, final Connection connection, final Cascade cascade) {
		super(connection, cascade);
		if (!(Cascade.DELETE == cascade || Cascade.NONE == cascade)) {
			throw new IllegalArgumentException("Only NONE and DELETE are allowed values for Cascade");
		}

		for (final Field field : ReflectionUtil.getAllFields(clazz)) {
			fieldList.add(field);
			if (List.class.isAssignableFrom(field.getType())) {
				prepareDelete(field);
				prepareSelect(field);
			}
		}

		final String deleteQuery = String.format(DELETE_TEMPLATE, clazz.getSimpleName());
		LOGGER.info(deleteQuery);
		try {
			preparedStatement = connection.prepareStatement(deleteQuery);
		} catch (final SQLException e) {
			throw new DatabaseException(e);
		}
	}

	/**
	 * Executes a prepared delete statement on element objectToDelete.
	 *
	 * @param objectToDelete Object that gets deleted @ SQLException in case of an
	 *                       error
	 */
	public void execute(final T objectToDelete) {
		Assertions.assertNotNull(objectToDelete, "Instance for delete should not be null");
		final UUID objectId = MappingHelper.getId(objectToDelete);
		Assertions.assertNotNull(objectId, "Object " + objectToDelete + " does not contain an id field");

		try {
			for (final Field field : fieldList) {
				final Object fieldElementToDelete = ReflectionUtil.executeGet(field, objectToDelete);

				if (List.class.isAssignableFrom(field.getType())) {
					handleList(field, fieldElementToDelete, objectId);
					continue;
				}

				final Object dbObject = MappingHelper.getDbObject(fieldElementToDelete);
				if (Cascade.DELETE == cascade && dbObject != fieldElementToDelete && UUID.class.isInstance(dbObject)) {
					delete(fieldElementToDelete, getConnection(), cascade);
				}
			}
			preparedStatement.setObject(1, objectId);
			preparedStatement.executeUpdate();
		} catch (final SQLException e) {
			throw new DatabaseException(e);
		}
	}

	private void handleList(final Field field, final Object fieldElementToDelete, final UUID objectId)
			throws SQLException {
		if (fieldElementToDelete == null) {
			return;
		}
		final PreparedStatement selectStatement = mappingSelect.get(field);

		final Set<Object> existingElements = new HashSet<>();
		selectStatement.setObject(1, objectId);
		LOGGER.info(selectStatement::toString);
		executeQuery(selectStatement, rs -> existingElements.add(rs.getObject(1)));

		final List<?> list = (List<?>) fieldElementToDelete;
		for (final Object listElement : list) {
			if (listElement == null) {
				continue;
			}

			if (!MappingHelper.isSupportedType(listElement.getClass())) {
				final Object element = MappingHelper.getId(listElement);
				if (existingElements.contains(element)) {
					if (Cascade.DELETE == cascade) {
						delete(listElement, getConnection(), cascade);
					}
				} else {
					throw new MappingException("Inconsistence found. The List " + field.toString()
							+ " contains a not persisted element " + element);
				}
			}
		}

		try (final PreparedStatement deleteStatement = mappingDeleteAll.get(field)) {
			deleteStatement.setObject(1, objectId);
			LOGGER.info(deleteStatement::toString);
			deleteStatement.execute();
		}
	}

	protected static <S> void delete(final S objectToDelete, final Connection connection, final Cascade cascade) {
		@SuppressWarnings("unchecked")
		final Class<S> clazz = (Class<S>) objectToDelete.getClass();
		final PreparedDelete<S> delete = new PreparedDelete<>(clazz, connection, cascade);
		delete.execute(objectToDelete);
	}
}
