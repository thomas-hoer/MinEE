package de.minee.jpa;

import de.minee.util.Assertions;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public abstract class AbstractPreparedQuery<T> extends AbstractQuery {

	private static final Logger LOGGER = Logger.getLogger(AbstractPreparedQuery.class.getName());

	protected final Cascade cascade;
	protected final Connection connection;

	protected final Map<Field, PreparedStatement> mappingSelect = new HashMap<>();
	protected final Map<Field, PreparedStatement> mappingInsert = new HashMap<>();
	protected final Map<Field, PreparedStatement> mappingDelete = new HashMap<>();
	protected final Map<Field, PreparedStatement> mappingDeleteAll = new HashMap<>();

	protected AbstractPreparedQuery(final Connection connection, final Cascade cascade) {
		Assertions.assertNotNull(connection);
		Assertions.assertNotNull(cascade);
		this.connection = connection;
		this.cascade = cascade;
	}

	protected final void prepareInsert(final Connection connection, final Field field) throws SQLException {
		final Class<?> cls = field.getDeclaringClass();
		mappingInsert.put(field, connection.prepareStatement(
				String.format("INSERT INTO Mapping_%s_%s VALUES (?,?)", cls.getSimpleName(), field.getName())));
	}

	protected final void prepareSelect(final Connection connection, final Field field) throws SQLException {
		final Class<?> cls = field.getDeclaringClass();
		final ParameterizedType mapToType = (ParameterizedType) field.getGenericType();
		final Class<?> type = (Class<?>) mapToType.getActualTypeArguments()[0];
		mappingSelect.put(field,
				connection.prepareStatement(String.format("SELECT %1$s FROM Mapping_%2$s_%3$s WHERE %2$s =?",
						type.getSimpleName(), cls.getSimpleName(), field.getName())));
	}

	protected final void prepareDelete(final Connection connection, final Field field) throws SQLException {
		final Class<?> cls = field.getDeclaringClass();
		final ParameterizedType mapToType = (ParameterizedType) field.getGenericType();
		final Class<?> type = (Class<?>) mapToType.getActualTypeArguments()[0];
		mappingDelete.put(field,
				connection.prepareStatement(String.format("DELETE FROM Mapping_%1$s_%2$s WHERE %1$s=? AND %3$s =?",
						cls.getSimpleName(), field.getName(), type.getSimpleName())));
		mappingDeleteAll.put(field, connection.prepareStatement(
				String.format("DELETE FROM Mapping_%1$s_%2$s WHERE %1$s=?", cls.getSimpleName(), field.getName())));

	}

	protected UUID handleId(final T objectToInsert, final Field field, final Object fieldElementToInsert) {
		UUID objectId;
		if (fieldElementToInsert == null) {
			objectId = UUID.randomUUID();
			ReflectionUtil.executeSet(field, objectToInsert, objectId);
		} else {
			objectId = (UUID) fieldElementToInsert;
		}
		return objectId;
	}

	protected interface ResultSetConsumer {
		void accept(ResultSet resultSet) throws SQLException;
	}

	void removeReferences(final Field field, final UUID objectId, final Set<Object> existingElements)
			throws SQLException {
		final PreparedStatement deleteStatement = mappingDelete.get(field);
		for (final Object element : existingElements) {
			deleteStatement.setObject(1, objectId);
			deleteStatement.setObject(2, element);
			LOGGER.info(deleteStatement::toString);
			deleteStatement.execute();
		}
	}

	@Override
	protected Connection getConnection() {
		return connection;
	}
}