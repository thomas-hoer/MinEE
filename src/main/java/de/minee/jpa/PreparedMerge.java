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
import java.util.StringJoiner;
import java.util.UUID;
import java.util.logging.Logger;

class PreparedMerge<S> extends AbstractPreparedQuery<S> {

	private static final Logger LOGGER = Logger.getLogger(PreparedMerge.class.getName());

	private static final String MERGE_TEMPLATE = "MERGE INTO %s (%s) VALUES (%s)";
	private final List<Field> fieldList = new ArrayList<>();
	private final PreparedStatement preparedStatement;

	/**
	 * Creates a PreparedStatement for a merge query.
	 *
	 * @param cls        Class corresponding to the table where a entry should be
	 *                   merged
	 * @param connection Database connection
	 * @param cascade    Rule how referenced objects should be threaded
	 * @throws SQLException SQLException in case of an error
	 */
	public PreparedMerge(final Class<S> cls, final Connection connection, final Cascade cascade) throws SQLException {
		super(connection, cascade);

		final StringJoiner fieldNames = new StringJoiner(",");
		final StringJoiner values = new StringJoiner(",");
		for (final Field field : ReflectionUtil.getAllFields(cls)) {
			fieldList.add(field);
			if (List.class.isAssignableFrom(field.getType())) {
				prepareInsert(connection, field);
				prepareDelete(connection, field);
				prepareSelect(connection, field);
				continue;
			}
			fieldNames.add(field.getName());
			values.add("?");
		}

		final String mergeQuery = String.format(MERGE_TEMPLATE, cls.getSimpleName(), fieldNames, values);
		LOGGER.info(mergeQuery);
		preparedStatement = connection.prepareStatement(mergeQuery);
	}

	private UUID execute(final S objectToMerge) throws SQLException {
		Assertions.assertNotNull(objectToMerge);
		int i = 1;
		UUID objectId = null;
		for (final Field field : fieldList) {
			Object fieldElementToMerge = ReflectionUtil.executeGet(field, objectToMerge);

			if (List.class.isAssignableFrom(field.getType())) {
				continue;
			}

			if ("id".equals(field.getName())) {
				objectId = handleId(objectToMerge, field, fieldElementToMerge);
				fieldElementToMerge = objectId;
			}

			final Object dbObject = MappingHelper.getDbObject(fieldElementToMerge);
			preparedStatement.setObject(i++, dbObject);
			if (Cascade.MERGE == cascade && dbObject != fieldElementToMerge && UUID.class.isInstance(dbObject)) {
				merge(fieldElementToMerge, connection, cascade);
			}
		}

		for (final Field field : fieldList) {
			if (List.class.isAssignableFrom(field.getType())) {
				final Object fieldElementToMerge = ReflectionUtil.executeGet(field, objectToMerge);
				handleList(field, fieldElementToMerge, objectId);
			}
		}
		preparedStatement.executeUpdate();
		return objectId;
	}

	private void handleList(final Field field, final Object fieldElementToMerge, final UUID objectId)
			throws SQLException {
		if (fieldElementToMerge == null) {
			return;
		}
		final PreparedStatement selectStatement = mappingSelect.get(field);
		final PreparedStatement insertStatement = mappingInsert.get(field);
		final Set<Object> existingElements = new HashSet<>();
		selectStatement.setObject(1, objectId);
		LOGGER.info(selectStatement::toString);
		executeQuery(selectStatement, rs -> existingElements.add(rs.getObject(1)));

		final List<?> list = (List<?>) fieldElementToMerge;
		for (final Object listElement : list) {
			if (listElement == null) {
				continue;
			}
			final Object element;
			if (MappingHelper.isSupportedType(listElement.getClass())) {
				element = listElement;
			} else {
				if (Cascade.MERGE == cascade) {
					merge(listElement, connection, cascade);
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

	protected static <T> UUID merge(final T objectToMerge, final Connection connection, final Cascade cascade)
			throws SQLException {
		@SuppressWarnings("unchecked")
		final Class<T> clazz = (Class<T>) objectToMerge.getClass();
		final PreparedMerge<T> merge = new PreparedMerge<>(clazz, connection, cascade);
		return merge.execute(objectToMerge);
	}
}
