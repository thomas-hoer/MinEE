package de.minee.jpa;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.logging.Logger;

import de.minee.util.Assertions;
import de.minee.util.Pair;
import de.minee.util.ReflectionUtil;

public class PreparedInsert<T> {

	private static final Logger logger = Logger.getLogger(PreparedInsert.class.getName());

	private final List<Field> fieldList = new ArrayList<>();
	private final Connection connection;
	private final PreparedStatement preparedStatement;
	private final Map<Field, PreparedStatement> mappingInsert = new HashMap<>();
	private final boolean deepInsert;

	public PreparedInsert(final Class<T> clazz, final Connection connection, final boolean deepInsert)
			throws SQLException {
		this.connection = connection;
		this.deepInsert = deepInsert;
		final StringBuilder query = new StringBuilder();
		final StringJoiner fieldNames = new StringJoiner(",");
		final StringJoiner values = new StringJoiner(",");

		query.append("INSERT INTO ");
		query.append(clazz.getSimpleName());

		final Field[] fields = clazz.getDeclaredFields();
		for (final Field field : fields) {
			field.setAccessible(true);
			fieldList.add(field);
			if (List.class.isAssignableFrom(field.getType())) {
				mappingInsert.put(field, connection.prepareStatement(
						"INSERT INTO Mapping_" + clazz.getSimpleName() + "_" + field.getName() + " VALUES (?,?)"));
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

		final String insertQuery = query.toString();
		logger.info(insertQuery);
		preparedStatement = connection.prepareStatement(insertQuery);
	}

	public UUID execute(final T objectToInsert) throws SQLException {
		Assertions.assertNotNull(objectToInsert);
		int i = 1;
		final List<Pair<Field, Object>> mappingsToInsert = new ArrayList<>();
		UUID objectId = null;
		for (final Field field : fieldList) {
			Object fieldElementToInsert = ReflectionUtil.executeGet(field, objectToInsert);
			if (List.class.isAssignableFrom(field.getType())) {
				handleList(mappingsToInsert, field, fieldElementToInsert);
				continue;
			}
			if ("id".equals(field.getName())) {
				objectId = handleId(objectToInsert, field, fieldElementToInsert);
				fieldElementToInsert = objectId;
			}
			final Object dbObject = MappingHelper.getDbObject(fieldElementToInsert);
			if (deepInsert && dbObject != fieldElementToInsert && UUID.class.isInstance(dbObject)) {
				final UUID id = insert(fieldElementToInsert, connection, deepInsert);
				preparedStatement.setObject(i++, id);
			} else {
				preparedStatement.setObject(i++, dbObject);
			}
		}
		logger.info(preparedStatement::toString);
		preparedStatement.execute();

		for (final Pair<Field, Object> mappingToInsert : mappingsToInsert) {
			final PreparedStatement preparedMappingStatement = mappingInsert.get(mappingToInsert.first());
			preparedMappingStatement.setObject(1, objectId);
			preparedMappingStatement.setObject(2, mappingToInsert.second());
			logger.info(preparedMappingStatement::toString);
			preparedMappingStatement.execute();
		}

		return objectId;
	}

	private UUID handleId(final T objectToInsert, final Field field, final Object fieldElementToInsert) {
		UUID objectId;
		if (fieldElementToInsert == null) {
			objectId = UUID.randomUUID();
			ReflectionUtil.executeSet(field, objectToInsert, objectId);
		} else {
			objectId = (UUID) fieldElementToInsert;
		}
		return objectId;
	}

	private void handleList(final List<Pair<Field, Object>> mappingsToInsert, final Field field,
			final Object fieldElementToInsert) throws SQLException {
		if (fieldElementToInsert == null) {
			return;
		}
		final List<?> list = (List<?>) fieldElementToInsert;
		for (final Object listElement : list) {
			if (listElement == null) {
				continue;
			}
			if (MappingHelper.isSupportedType(listElement.getClass())) {
				mappingsToInsert.add(new Pair<>(field, listElement));
			} else {
				final UUID insertId;
				if (deepInsert) {
					insertId = insert(listElement, connection, deepInsert);
				} else {
					insertId = MappingHelper.getId(listElement);
				}
				if (insertId != null) {
					mappingsToInsert.add(new Pair<>(field, insertId));
				}
			}
		}
	}

	protected static <S> UUID insert(final S objectToInsert, final Connection connection, final boolean deepInsert)
			throws SQLException {
		final Class<S> clazz = (Class<S>) objectToInsert.getClass();
		final PreparedInsert<S> insert = new PreparedInsert<>(clazz, connection, deepInsert);
		return insert.execute(objectToInsert);
	}
}
