package de.minee.jpa;

import de.minee.util.Assertions;
import de.minee.util.Logger;
import de.minee.util.Pair;
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

public class PreparedInsert<T> extends AbstractPreparedQuery<T> {

	private static final Logger LOGGER = Logger.getLogger(PreparedInsert.class);

	private static final String INSERT_TEMPLATE = "INSERT INTO %s (%s) VALUES (%s)";
	private final List<Field> fieldList = new ArrayList<>();
	private final PreparedStatement preparedStatement;

	/**
	 * Creates a PreparedStatement for a insert query.
	 *
	 * @param cls        Class corresponding to the table where a entry should be
	 *                   inserted
	 * @param connection Database connection
	 * @param cascade    Rule how referenced objects should be threaded @
	 *                   SQLException in case of an error
	 */
	public PreparedInsert(final Class<T> cls, final Connection connection, final Cascade cascade) {
		super(connection, cascade);

		final StringJoiner fieldNames = new StringJoiner(",");
		final StringJoiner values = new StringJoiner(",");
		for (final Field field : ReflectionUtil.getAllFields(cls)) {
			fieldList.add(field);
			if (List.class.isAssignableFrom(field.getType())) {
				prepareInsert(field);
				continue;
			}
			fieldNames.add(field.getName());
			values.add("?");
		}

		final String insertQuery = String.format(INSERT_TEMPLATE, cls.getSimpleName(), fieldNames, values);
		LOGGER.info(insertQuery);
		preparedStatement = prepare(insertQuery);
	}

	/**
	 * Executes the insert of the object objectToInsert and persists it in the
	 * Database.
	 *
	 * @param objectToInsert Object that should be persisted
	 * @param handledObjects Object cache of inserted entries in the same session
	 * @return Id of the inserted Object @ SQLException in case of an error
	 */
	private UUID execute(final T objectToInsert, final Set<Object> handledObjects) throws SQLException {
		Assertions.assertNotNull(objectToInsert, "Instance for insert should not be null");
		if (handledObjects.contains(objectToInsert)) {
			return MappingHelper.getId(objectToInsert);
		}
		handledObjects.add(objectToInsert);
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
			if (Cascade.INSERT == cascade && dbObject != fieldElementToInsert && UUID.class.isInstance(dbObject)) {
				final UUID id = insert(fieldElementToInsert, connection, cascade, handledObjects);
				preparedStatement.setObject(i++, id);
			} else {
				preparedStatement.setObject(i++, dbObject);
			}
		}
		LOGGER.info(preparedStatement::toString);
		preparedStatement.execute();

		for (final Pair<Field, Object> mappingToInsert : mappingsToInsert) {
			final PreparedStatement preparedMappingStatement = mappingInsert.get(mappingToInsert.first());
			preparedMappingStatement.setObject(1, objectId);
			preparedMappingStatement.setObject(2, mappingToInsert.second());
			LOGGER.info(preparedMappingStatement::toString);
			preparedMappingStatement.execute();
		}
		return objectId;
	}

	private void handleList(final List<Pair<Field, Object>> mappingsToInsert, final Field field,
			final Object fieldElementToInsert) {
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
			}else if( listElement.getClass().isEnum()) {
				mappingsToInsert.add(new Pair<>(field, listElement.toString()));
			} else {
				final UUID insertId;
				if (Cascade.INSERT == cascade) {
					insertId = insert(listElement, connection, cascade);
				} else {
					insertId = MappingHelper.getId(listElement);
				}
				if (insertId != null) {
					mappingsToInsert.add(new Pair<>(field, insertId));
				}
			}
		}
	}

	protected static <S> UUID insert(final S objectToInsert, final Connection connection, final Cascade cascade) {
		@SuppressWarnings("unchecked")
		final Class<S> clazz = (Class<S>) objectToInsert.getClass();
		final PreparedInsert<S> insert = new PreparedInsert<>(clazz, connection, cascade);
		try {
			return insert.execute(objectToInsert, new HashSet<>());
		} catch (final SQLException e) {
			throw new DatabaseException(e);
		}
	}

	private static <S> UUID insert(final S objectToInsert, final Connection connection, final Cascade cascade,
			final Set<Object> handledObjects) {
		@SuppressWarnings("unchecked")
		final Class<S> clazz = (Class<S>) objectToInsert.getClass();
		final PreparedInsert<S> insert = new PreparedInsert<>(clazz, connection, cascade);
		try {
			return insert.execute(objectToInsert, handledObjects);
		} catch (final SQLException e) {
			throw new DatabaseException(e);
		}
	}

}
