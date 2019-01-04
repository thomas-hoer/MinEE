package de.minee.jpa;

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

import de.minee.util.Assertions;
import de.minee.util.ReflectionUtil;

class PreparedMerge<S> extends PreparedQueryBase<S> {

	private static final Logger logger = Logger.getLogger(PreparedMerge.class.getName());

	private final List<Field> fieldList = new ArrayList<>();
	private final PreparedStatement preparedStatement;

	public PreparedMerge(final Class<S> clazz, final Connection connection, final Cascade cascade) throws SQLException {
		super(connection, cascade);
		final StringBuilder query = new StringBuilder();
		final StringJoiner fieldNames = new StringJoiner(",");
		final StringJoiner values = new StringJoiner(",");

		query.append("MERGE INTO ");
		query.append(clazz.getSimpleName());

		final Field[] fields = clazz.getDeclaredFields();
		for (final Field field : fields) {
			field.setAccessible(true);
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
		query.append("(");
		query.append(fieldNames.toString());
		query.append(") VALUES (");
		query.append(values.toString());
		query.append(")");

		final String mergeQuery = query.toString();
		logger.info(mergeQuery);
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
			if (Cascade.MERGE.equals(cascade) && dbObject != fieldElementToMerge && UUID.class.isInstance(dbObject)) {
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

		final List<?> list = (List<?>) fieldElementToMerge;
		for (final Object listElement : list) {
			if (listElement == null) {
				continue;
			}
			final Object element;
			if (MappingHelper.isSupportedType(listElement.getClass())) {
				element = listElement;
			} else {
				if (Cascade.MERGE.equals(cascade)) {
					merge(listElement, connection, cascade);
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

	protected static <T> UUID merge(final T objectToMerge, final Connection connection, final Cascade cascade)
			throws SQLException {
		@SuppressWarnings("unchecked")
		final Class<T> clazz = (Class<T>) objectToMerge.getClass();
		final PreparedMerge<T> merge = new PreparedMerge<>(clazz, connection, cascade);
		return merge.execute(objectToMerge);
	}
}