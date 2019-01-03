package de.minee.jpa;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Logger;

import de.minee.util.Assertions;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

public class SelectStatement<T> {

	private static final Logger logger = Logger.getLogger(SelectStatement.class.getName());

	private final ProxyFactory proxyFactory;

	private final Class<T> clazz;
	private final Connection connection;
	private String id = null;
	private final List<WhereClause<?, ?>> whereClauses = new ArrayList<>();
	private final List<Field> fieldList = new ArrayList<>();
	private String additionalWhereClause;

	public SelectStatement(final Class<T> clazz, final Connection connection) {
		Assertions.assertNotNull(clazz);
		Assertions.assertNotNull(connection);
		this.clazz = clazz;
		this.connection = connection;
		for (final Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			fieldList.add(field);
		}
		proxyFactory = new ProxyFactory();
		proxyFactory.setSuperclass(clazz);
	}

	public SelectStatement<T> id(final String id) {
		Assertions.assertNotNull(id);
		if (this.id != null) {
			throw new MappingException("Id already set. Please don't call this method twice.");
		}
		this.id = id;
		return this;
	}

	public <S> WhereClause<S, T> where(final Function<T, S> whereField) throws SQLException {
		final WhereClause<S, T> where = new WhereClause<>(whereField, this);
		whereClauses.add(where);
		return where;
	}

	public T byId(final UUID id) throws SQLException {
		final StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ");
		query.append(clazz.getSimpleName());
		query.append(" WHERE id = '");
		query.append(id.toString());
		query.append("'");
		final String selectQuery = query.toString();
		logger.info(selectQuery);
		try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(selectQuery);) {
			return rs.next() ? mapResultSet(rs) : null;
		}
	}

	public List<T> execute(final Collection<?> args) throws SQLException {
		final String query = assembleQuery();
		logger.info(query);

		final List<T> resultList = new ArrayList<>();
		ResultSet rs = null;
		try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
			int i = 1;
			for (final Object arg : args) {
				preparedStatement.setObject(i++, arg);
			}
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				resultList.add(mapResultSet(rs));
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
		return resultList;
	}

	public List<T> execute() throws SQLException {
		final String query = assembleQuery();
		logger.info(query);
		final List<T> resultList = new ArrayList<>();
		try (final Statement statement = connection.createStatement();
				final ResultSet rs = statement.executeQuery(query);) {
			while (rs.next()) {
				resultList.add(mapResultSet(rs));
			}
		}
		return resultList;
	}

	private String assembleQuery() {
		final StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ");
		query.append(clazz.getSimpleName());
		query.append(" ");
		for (final WhereClause<?, ?> whereClause : whereClauses) {
			query.append(whereClause.getJoinClause());
		}
		if (!whereClauses.isEmpty() || (additionalWhereClause != null && !"".equals(additionalWhereClause))) {
			query.append("WHERE ");
		}
		final StringJoiner stringJoiner = new StringJoiner(" AND ");
		if (additionalWhereClause != null) {
			stringJoiner.add(additionalWhereClause);
		}
		for (final WhereClause<?, ?> whereClause : whereClauses) {
			stringJoiner.add(whereClause.toString());
		}
		query.append(stringJoiner.toString());
		return query.toString();
	}

	private T mapResultSet(final ResultSet rs) throws SQLException {
		try {
			final T obj = clazz.newInstance();
			for (final Field field : fieldList) {
				if (List.class.isAssignableFrom(field.getType())) {
					handleList(obj, field);
					continue;
				}
				handleFieldColumn(field, rs, obj);
			}
			return obj;
		} catch (IllegalAccessException | InstantiationException e) {
			throw new SQLException("Cannot instanciate object of type " + clazz.getName(), e);
		}
	}

	private void handleFieldColumn(final Field field, final ResultSet rs, final T obj)
			throws SQLException, IllegalAccessException {
		final Object value = rs.getObject(field.getName());
		if (value != null) {
			if (value.getClass().isAssignableFrom(field.getType())) {
				field.set(obj, value);
			} else if (UUID.class.equals(value.getClass())) {
				final Object resolvedValue = select(field.getType(), connection).byId((UUID) value);
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

	private void handleList(final T obj, final Field field) throws SQLException, IllegalAccessException {
		final ParameterizedType mapToType = (ParameterizedType) field.getGenericType();
		final Class<?> type = (Class<?>) mapToType.getActualTypeArguments()[0];
		final boolean supportedType = MappingHelper.isSupportedType(type);
		final List<Object> list = new ArrayList<>();
		try (Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("SELECT " + type.getSimpleName() + " FROM Mapping_"
						+ clazz.getSimpleName() + "_" + field.getName());) {
			while (resultSet.next()) {
				if (supportedType) {
					list.add(resultSet.getObject(1));
				} else {
					final Object o = select(type, connection).byId((UUID) resultSet.getObject(1));
					list.add(o);
				}
			}
		}
		field.set(obj, list);
	}

	protected static <S> SelectStatement<S> select(final Class<S> clazz, final Connection connection) {
		return new SelectStatement<>(clazz, connection);
	}

	@SuppressWarnings("unchecked")
	protected T getProxy() throws SQLException {
		final MethodHandler handler = new ProxyMethodHandler();
		try {
			final Object proxy = proxyFactory.create(new Class<?>[0], new Object[0]);
			((Proxy) proxy).setHandler(handler);
			return (T) proxy;
		} catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException
				| InvocationTargetException e) {
			throw new SQLException("Cannot instantiate Object of type " + clazz.getName(), e);
		}
	}

	protected Class<T> getType() {
		return clazz;
	}

	private class ProxyMethodHandler implements MethodHandler {

		private String lastCalledMethod;

		@Override
		public Object invoke(final Object self, final Method thisMethod, final Method proceed, final Object[] args)
				throws Throwable {
			final String methodName = thisMethod.getName();
			if ("toString".equals(methodName)) {
				return lastCalledMethod;
			}
			lastCalledMethod = methodName.substring(3);
			return null;
		}

	}

	public SelectStatement<T> query(final String whereClause) {
		additionalWhereClause = whereClause;
		return this;
	}

}
