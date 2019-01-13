package de.minee.jpa;

import de.minee.util.Assertions;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class PreparedDelete<T> extends AbstractPreparedQuery<T> {

	private static final Logger LOGGER = Logger.getLogger(PreparedDelete.class.getName());

	private final List<Field> fieldList = new ArrayList<>();
	private final PreparedStatement preparedStatement;

	/**
	 * Creates a PreparedStatement for a delete query.
	 *
	 * @param clazz      Class corresponding to the table where a entry should be
	 *                   deleted
	 * @param connection Database connection
	 * @param cascade    Rule how referenced objects should be threaded
	 * @throws SQLException SQLException in case of an error
	 */
	public PreparedDelete(final Class<T> clazz, final Connection connection, final Cascade cascade)
			throws SQLException {
		super(connection, cascade);
		if (!(Cascade.DELETE == cascade || Cascade.NONE == cascade)) {
			throw new IllegalArgumentException("Only NONE and DELETE are allowed values for Cascade");
		}
		final StringBuilder query = new StringBuilder();

		query.append("DELETE FROM ");
		query.append(clazz.getSimpleName());
		query.append(" WHERE id = ?");

		for (final Field field : ReflectionUtil.getAllFields(clazz)) {
			fieldList.add(field);
			if (List.class.isAssignableFrom(field.getType())) {
				prepareDelete(connection, field);
				prepareSelect(connection, field);
			}
		}

		final String deleteQuery = query.toString();
		LOGGER.info(deleteQuery);
		preparedStatement = connection.prepareStatement(deleteQuery);
	}

	/**
	 * Executes a prepared delete statement on element objectToDelete.
	 *
	 * @param objectToDelete Object that gets deleted
	 * @throws SQLException SQLException in case of an error
	 */
	public void execute(final T objectToDelete) throws SQLException {
		Assertions.assertNotNull(objectToDelete);
		final UUID objectId = MappingHelper.getId(objectToDelete);
		Assertions.assertNotNull(objectId);

		for (final Field field : fieldList) {
			final Object fieldElementToDelete = ReflectionUtil.executeGet(field, objectToDelete);

			if (List.class.isAssignableFrom(field.getType())) {
				handleList(field, fieldElementToDelete, objectId);
				continue;
			}

			final Object dbObject = MappingHelper.getDbObject(fieldElementToDelete);
			if (Cascade.DELETE == cascade && dbObject != fieldElementToDelete && UUID.class.isInstance(dbObject)) {
				delete(fieldElementToDelete, connection, cascade);
			}
		}

		preparedStatement.setObject(1, objectId);
		preparedStatement.executeUpdate();
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
						delete(listElement, connection, cascade);
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

	protected static <S> void delete(final S objectToDelete, final Connection connection, final Cascade cascade)
			throws SQLException {
		@SuppressWarnings("unchecked")
		final Class<S> clazz = (Class<S>) objectToDelete.getClass();
		final PreparedDelete<S> delete = new PreparedDelete<>(clazz, connection, cascade);
		delete.execute(objectToDelete);
	}
}
