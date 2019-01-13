package de.minee.jpa;

import de.minee.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SelectStatement<T> extends AbstractStatement<T> {

	public SelectStatement(final Class<T> clazz, final Connection connection) {
		super(clazz, connection);
	}

	protected static <S> AbstractStatement<S> select(final Class<S> clazz, final Connection connection) {
		return new SelectStatement<>(clazz, connection);
	}

	@Override
	void handleFieldColumn(final Field field, final ResultSet rs, final T obj, final Map<Object, Object> handledObjects)
			throws SQLException {
		final Object value = rs.getObject(field.getName());
		if (value != null) {
			if (value.getClass().isAssignableFrom(field.getType())) {
				ReflectionUtil.executeSet(field, obj, value);
			} else if (UUID.class.equals(value.getClass())) {
				final Object resolvedValue = select(field.getType(), getConnection()).byId((UUID) value,
						handledObjects);
				ReflectionUtil.executeSet(field, obj, resolvedValue);
			} else if (field.getType().isEnum()) {
				final String stringValue = rs.getString(field.getName());
				ReflectionUtil.executeSet(field, obj, MappingHelper.getEnum(field.getType(), stringValue));
			} else if (field.getType().isArray()) {
				if (byte.class.equals(field.getType().getComponentType())) {
					ReflectionUtil.executeSet(field, obj, Base64.getDecoder().decode((String) value));
				} else {
					ReflectionUtil.executeSet(field, obj, rs.getArray(field.getName()).getArray());
				}
			} else {
				throw new SQLException("Cannot set value of type " + value.getClass() + " to " + field.getName()
						+ " of type " + field.getType());
			}
		}
	}

	@Override
	void handleList(final T obj, final Field field, final Map<Object, Object> handledObjects) throws SQLException {
		final ParameterizedType mapToType = (ParameterizedType) field.getGenericType();
		final Class<?> type = (Class<?>) mapToType.getActualTypeArguments()[0];
		final boolean supportedType = MappingHelper.isSupportedType(type);
		final List<Object> list = new ArrayList<>();
		final String query = "SELECT " + type.getSimpleName() + " FROM Mapping_" + getType().getSimpleName() + "_"
				+ field.getName();
		executeQuery(query, resultSet -> {
			if (supportedType) {
				list.add(resultSet.getObject(1));
			} else {
				final Object o = select(type, getConnection()).byId((UUID) resultSet.getObject(1), handledObjects);
				list.add(o);
			}
		});
		ReflectionUtil.executeSet(field, obj, list);
	}
}
