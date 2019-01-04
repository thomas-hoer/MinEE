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
import java.util.UUID;
import java.util.logging.Logger;

import de.minee.util.Assertions;
import de.minee.util.ReflectionUtil;

public class PreparedDelete<T> extends PreparedQueryBase<T> {

	private static final Logger logger = Logger.getLogger(PreparedDelete.class.getName());

	private final List<Field> fieldList = new ArrayList<>();
	private final PreparedStatement preparedStatement;

	public PreparedDelete(final Class<T> clazz, final Connection connection, final Cascade cascade)
			throws SQLException {
		super(connection, cascade);
		if (!(Cascade.DELETE.equals(cascade) || Cascade.NONE.equals(cascade))) {
			throw new IllegalArgumentException("Only NONE and DELETE are allowed values for Cascade");
		}
		final StringBuilder query = new StringBuilder();

		query.append("DELETE FROM ");
		query.append(clazz.getSimpleName());
		query.append(" WHERE id = ?");

		final Field[] fields = clazz.getDeclaredFields();
		for (final Field field : fields) {
			field.setAccessible(true);
			fieldList.add(field);
			if (List.class.isAssignableFrom(field.getType())) {
				prepareDelete(connection, field);
				prepareSelect(connection, field);
			}
		}

		final String deleteQuery = query.toString();
		logger.info(deleteQuery);
		preparedStatement = connection.prepareStatement(deleteQuery);
	}

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
			if (Cascade.DELETE.equals(cascade) && dbObject != fieldElementToDelete && UUID.class.isInstance(dbObject)) {
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
		final PreparedStatement deleteStatement = mappingDeleteAll.get(field);

		final Set<Object> existingElements = new HashSet<>();
		selectStatement.setObject(1, objectId);
		logger.info(selectStatement::toString);
		try (ResultSet rs = selectStatement.executeQuery()) {
			while (rs.next()) {
				existingElements.add(rs.getObject(1));
			}
		}

		final List<?> list = (List<?>) fieldElementToDelete;
		for (final Object listElement : list) {
			if (listElement == null) {
				continue;
			}

			if (!MappingHelper.isSupportedType(listElement.getClass())) {
				final Object element = MappingHelper.getId(listElement);
				if (existingElements.contains(element)) {
					if (Cascade.DELETE.equals(cascade)) {
						delete(listElement, connection, cascade);
					}
				} else {
					throw new MappingException("Inconsistence found. The List " + field.toString()
							+ " contains a not persisted element " + element);
				}
			}
		}

		deleteStatement.setObject(1, objectId);
		logger.info(deleteStatement::toString);
		deleteStatement.execute();
	}

	protected static <S> void delete(final S objectToDelete, final Connection connection, final Cascade cascade)
			throws SQLException {
		@SuppressWarnings("unchecked")
		final Class<S> clazz = (Class<S>) objectToDelete.getClass();
		final PreparedDelete<S> delete = new PreparedDelete<>(clazz, connection, cascade);
		delete.execute(objectToDelete);
	}
}
