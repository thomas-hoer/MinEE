package de.minee.jpa;

import de.minee.env.Environment;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

public abstract class AbstractDAO {

	private static final Logger LOGGER = Logger.getLogger(AbstractDAO.class.getName());
	private static final Map<String, Connection> connections = new HashMap<>();
	private Connection localConnection;
	private Statement statementForSchemaUpdate;

	protected String getConnectionString() {
		return Environment.getEnvironmentVariable("DB_CONNECTION");
	}

	protected String getUserName() {
		return Environment.getEnvironmentVariable("DB_USER");
	}

	protected String getPassword() {
		return Environment.getEnvironmentVariable("DB_PASSWORD");
	}

	protected abstract int updateDatabaseSchema(int oldDbSchemaVersion) throws SQLException;

	private synchronized Connection createConnection() throws SQLException {
		final String connectionString = getConnectionString();
		if (connections.containsKey(connectionString) && !"jdbc:h2:mem:".equals(connectionString)) {
			return connections.get(connectionString);
		}
		final Connection connection = DriverManager.getConnection(connectionString, getUserName(), getPassword());
		checkVersion(connection);
		connections.put(connectionString, connection);
		return connection;
	}

	protected Connection getConnection() throws SQLException {
		if (localConnection == null) {
			localConnection = createConnection();
		}
		return localConnection;
	}

	private void checkVersion(final Connection con) throws SQLException {
		int dbSchemaVersion = 0;
		try (Statement statement = con.createStatement();
				ResultSet resultSetCustomProperties = statement.executeQuery(
						"SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CUSTOMPROPERTY'")) {
			resultSetCustomProperties.next();
			final boolean tableFound = resultSetCustomProperties.getInt(1) == 1;
			if (!tableFound) {
				statement.executeUpdate("CREATE TABLE CustomProperty ( Key varchar(50), Value varchar(50) )");
				statement.executeUpdate("INSERT INTO CustomProperty ( Key , Value ) VALUES ( 'dbShemaVersion','0') ");
			}
			try (final ResultSet resultSetDbSchemaVersion = statement
					.executeQuery("SELECT Value FROM CustomProperty WHERE Key = 'dbShemaVersion'")) {
				resultSetDbSchemaVersion.next();
				dbSchemaVersion = resultSetDbSchemaVersion.getInt(1);
			}

			statementForSchemaUpdate = statement;
			dbSchemaVersion = updateDatabaseSchema(dbSchemaVersion);
			statementForSchemaUpdate = null;
		}

		try (PreparedStatement preparedStatement = con
				.prepareStatement("UPDATE CustomProperty SET Value = ? WHERE Key = ?")) {
			preparedStatement.setInt(1, dbSchemaVersion);
			preparedStatement.setString(2, "dbShemaVersion");
			preparedStatement.execute();
		}
	}

	protected void dropTable(final Class<?> cls) throws SQLException {
		dropTable(cls.getSimpleName());
	}

	protected void dropTable(final String table) throws SQLException {
		if (statementForSchemaUpdate == null) {
			throw new SQLException("dropTable is only allowed during updateDatabaseSchema process");
		}
		final String dropTableQuery = String.format("DROP TABLE %s", table);
		LOGGER.info(dropTableQuery);
		statementForSchemaUpdate.execute(dropTableQuery);
	}

	protected void createTable(final Class<?> cls) throws SQLException {
		if (statementForSchemaUpdate == null) {
			throw new SQLException("createTable is only allowed during updateDatabaseSchema process");
		}
		final StringBuilder stringBuilder = new StringBuilder();
		final List<Field> fields = ReflectionUtil.getAllFields(cls);
		stringBuilder.append("CREATE TABLE ");
		stringBuilder.append(cls.getSimpleName());
		stringBuilder.append("(");

		for (final Field field : fields) {
			final String mappedType = MappingHelper.mapDatabaseType(field);
			if (mappedType != null) {
				stringBuilder.append(field.getName());
				stringBuilder.append(" ");
				stringBuilder.append(mappedType);
				if ("id".equals(field.getName())) {
					stringBuilder.append(" PRIMARY KEY");
				}
				stringBuilder.append(",");
			} else {
				createMappingTableFor(field, statementForSchemaUpdate);
			}
		}
		stringBuilder.setLength(stringBuilder.length() - 1);
		stringBuilder.append(")");
		final String createTableQuery = stringBuilder.toString();
		LOGGER.info(createTableQuery);
		statementForSchemaUpdate.execute(createTableQuery);
	}

	protected void updateTable(final Class<?> cls) throws SQLException {
		updateTable(cls, false);
	}

	protected void updateTable(final Class<?> cls, final boolean allowDeletion) throws SQLException {
		if (statementForSchemaUpdate == null) {
			throw new SQLException("createTable is only allowed during updateDatabaseSchema process");
		}
		final Map<String, String> existingFields = analyseExistingFieldsForTable(cls);

		final List<Field> fields = ReflectionUtil.getAllFields(cls);
		for (final Field field : fields) {
			final String mappedType = MappingHelper.mapDatabaseType(field);

			final String fieldName = field.getName().toUpperCase();
			if (existingFields.containsKey(fieldName)) {
				if (existingFields.get(fieldName).equalsIgnoreCase(mappedType)) {
					existingFields.remove(fieldName);
				} else if (List.class.isAssignableFrom(field.getType())
						&& "List".equals(existingFields.get(fieldName))) {
					existingFields.remove(fieldName);
				} else {
					throw new UnsupportedOperationException("Cannot change field type");
				}
			} else {
				alterTableAddField(cls, field, mappedType);
			}
		}

		for (final Entry<String, String> entry : existingFields.entrySet()) {
			if (allowDeletion) {
				alterTableDropField(cls, entry);
			} else {
				LOGGER.warning("Warning: Field " + entry.getKey() + " of class " + cls.getSimpleName() + " is unused");
			}
		}

	}

	private Map<String, String> analyseExistingFieldsForTable(final Class<?> cls) throws SQLException {
		final Map<String, String> existingFields = new HashMap<>();
		try (ResultSet resultSetColumns = statementForSchemaUpdate.executeQuery(
				"SELECT COLUMNS.COLUMN_NAME, COLUMNS.TYPE_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE COLUMNS.TABLE_NAME = '"
						+ cls.getSimpleName().toUpperCase() + "'")) {
			while (resultSetColumns.next()) {
				existingFields.put(resultSetColumns.getString(1), resultSetColumns.getString(2));
			}
		}
		try (ResultSet resultSetMappingTable = statementForSchemaUpdate
				.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLES.TABLE_NAME LIKE 'MAPPING_"
						+ cls.getSimpleName().toUpperCase() + "_%'")) {
			while (resultSetMappingTable.next()) {
				final String tableName = resultSetMappingTable.getString(1)
						.replace("MAPPING_" + cls.getSimpleName().toUpperCase() + "_", "");
				existingFields.put(tableName, "List");
			}
		}
		return existingFields;
	}

	private void alterTableDropField(final Class<?> cls, final Entry<String, String> entry) throws SQLException {
		final StringBuilder query = new StringBuilder();
		query.append("ALTER TABLE ");
		query.append(cls.getSimpleName());
		query.append(" DROP COLUMN ");
		query.append(entry.getKey());
		query.append(";");
		final String alterTableQuery = query.toString();
		LOGGER.info(alterTableQuery);
		statementForSchemaUpdate.execute(alterTableQuery);
	}

	private void alterTableAddField(final Class<?> cls, final Field field, final String mappedType)
			throws SQLException {
		if (mappedType != null) {
			final StringBuilder query = new StringBuilder();
			query.append("ALTER TABLE ");
			query.append(cls.getSimpleName());
			query.append(" ADD COLUMN ");
			query.append(field.getName());
			query.append(" ");
			query.append(mappedType);
			query.append(";");
			final String alterTableQuery = query.toString();
			LOGGER.info(alterTableQuery);
			statementForSchemaUpdate.execute(alterTableQuery);
		} else {
			createMappingTableFor(field, statementForSchemaUpdate);
		}
	}

	private static void createMappingTableFor(final Field field, final Statement statement) throws SQLException {
		final Class<?> fromClazz = field.getDeclaringClass();

		final ParameterizedType mapToType = (ParameterizedType) field.getGenericType();
		final Class<?> type = (Class<?>) mapToType.getActualTypeArguments()[0];

		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("CREATE TABLE ");
		stringBuilder.append("Mapping_");
		stringBuilder.append(fromClazz.getSimpleName());
		stringBuilder.append("_");
		stringBuilder.append(field.getName());
		stringBuilder.append("(");
		stringBuilder.append(fromClazz.getSimpleName());
		stringBuilder.append(" UUID, ");
		if (MappingHelper.isSupportedType(type)) {
			stringBuilder.append(type.getSimpleName());
			stringBuilder.append(" ");
			stringBuilder.append(MappingHelper.mapType(type));
		} else {
			stringBuilder.append(type.getSimpleName());
			stringBuilder.append(" UUID");
			stringBuilder.append(", PRIMARY KEY (");
			stringBuilder.append(fromClazz.getSimpleName());
			stringBuilder.append(", ");
			stringBuilder.append(type.getSimpleName());
			stringBuilder.append(")");
		}
		stringBuilder.append(")");

		final String createTableQuery = stringBuilder.toString();
		LOGGER.info(createTableQuery);
		statement.execute(createTableQuery);

	}

	/**
	 * Creates a Query for selecting Objects of type clazz.
	 *
	 * @param clazz Class corresponding to a DB Table
	 * @return Fluent style based query builder
	 * @throws SQLException SQLException in case of an error
	 */
	public <T> InitialQueryConnection<T, AbstractStatement<T>> select(final Class<T> clazz) throws SQLException {
		final AbstractStatement<T> statement = SelectStatement.select(clazz, getConnection());
		return new InitialQueryConnection<>(statement, getConnection());
	}

	public <T> UUID insertShallow(final T objectToInsert) throws SQLException {
		return PreparedInsert.insert(objectToInsert, getConnection(), Cascade.NONE);
	}

	/**
	 * Deep insert of the Object and recursively their children.
	 *
	 * @param objectToInsert object that should be persisted
	 * @return id of the inserted object
	 * @throws SQLException SQLException in case of an error
	 */
	public <T> UUID insert(final T objectToInsert) throws SQLException {
		return PreparedInsert.insert(objectToInsert, getConnection(), Cascade.INSERT);
	}

	public <T> int update(final T objectToUpdate) throws SQLException {
		return PreparedUpdate.update(objectToUpdate, getConnection(), Cascade.UPDATE);
	}

	public <T> int updateShallow(final T objectToUpdate) throws SQLException {
		return PreparedUpdate.update(objectToUpdate, getConnection(), Cascade.NONE);
	}

	public <T> UUID merge(final T objectToMerge) throws SQLException {
		return PreparedMerge.merge(objectToMerge, getConnection(), Cascade.MERGE);
	}

	/**
	 * Directly deletes a Object and recursively its children.
	 *
	 * @param objectToDelete Object that shall be deleted from Database
	 * @throws SQLException SQLException in case of an error
	 */
	public <T> void delete(final T objectToDelete) throws SQLException {
		PreparedDelete.delete(objectToDelete, getConnection(), Cascade.DELETE);
	}

	/**
	 * Deletes a Object. Referenced Objects will not be deleted.
	 *
	 * @param objectToDelete Object that shall be deleted from Database
	 * @throws SQLException SQLException in case of an error
	 */
	public <T> void deleteShallow(final T objectToDelete) throws SQLException {
		PreparedDelete.delete(objectToDelete, getConnection(), Cascade.NONE);
	}
}
