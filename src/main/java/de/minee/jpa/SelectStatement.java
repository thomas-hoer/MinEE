package de.minee.jpa;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class SelectStatement<T> extends AbstractStatement<T> {

	public SelectStatement(final Class<T> clazz, final Connection connection) {
		super(clazz, connection);
	}

	protected static <S> AbstractStatement<S> select(final Class<S> clazz, final Connection connection) {
		return new SelectStatement<>(clazz, connection);
	}

	@Override
	void handleFieldColumn(final Field field, final ResultSet rs, final T obj)
			throws SQLException, IllegalAccessException {
		final Object value = rs.getObject(field.getName());
		if (value != null) {
			if (value.getClass().isAssignableFrom(field.getType())) {
				field.set(obj, value);
			} else if (UUID.class.equals(value.getClass())) {
				final Object resolvedValue = select(field.getType(), getConnection()).byId((UUID) value);
				field.set(obj, resolvedValue);
			} else if (field.getType().isEnum()) {
				final String stringValue = rs.getString(field.getName());
				field.set(obj, MappingHelper.getEnum(field.getType(), stringValue));
			} else if (field.getType().isArray()) {
				if (byte.class.equals(field.getType().getComponentType())) {
					field.set(obj, Base64.getDecoder().decode((String) value));
				} else {
					field.set(obj, rs.getArray(field.getName()).getArray());
				}
			} else {
				throw new SQLException("Cannot set value of type " + value.getClass() + " to " + field.getName()
						+ " of type " + field.getType());
			}
		}
	}

	@Override
	void handleList(final T obj, final Field field) throws SQLException, IllegalAccessException {
		final ParameterizedType mapToType = (ParameterizedType) field.getGenericType();
		final Class<?> type = (Class<?>) mapToType.getActualTypeArguments()[0];
		final boolean supportedType = MappingHelper.isSupportedType(type);
		final List<Object> list = new ArrayList<>();
		try (Statement statement = getConnection().createStatement();
				ResultSet resultSet = statement.executeQuery("SELECT " + type.getSimpleName() + " FROM Mapping_"
						+ getType().getSimpleName() + "_" + field.getName())) {
			while (resultSet.next()) {
				if (supportedType) {
					list.add(resultSet.getObject(1));
				} else {
					final Object o = select(type, getConnection()).byId((UUID) resultSet.getObject(1));
					list.add(o);
				}
			}
		}
		field.set(obj, list);
	}
}
