package de.minee.jpa;

import de.minee.env.Environment;
import de.minee.util.ReflectionUtil;
import de.minee.util.logging.Logger;

import java.lang.reflect.Field;
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

public abstract class AbstractDAO {

	private static final Logger LOGGER = Logger.getLogger(AbstractDAO.class);
	private static final Map<String, Connection> connections = new HashMap<>();
	private Connection localConnection;
	private Connection tempConnection;
	private Statement statementForSchemaUpdate;

	protected String getConnectionString() {
		return Environment.getEnvironmentVariable("DB_CONNECTION");
	}

	protected String getUserName() {
		return null;
	}

	protected String getPassword() {
		return null;
	}

	/**
	 * If the database is newly created oldDbSchemaVersion will initially be 0. It
	 * is not allowed to change the data through the fluent api directly during the
	 * updateDatabaseSchema process. However if you need to modify data use
	 * updateData() within this method. Data changes will be executed synchronously.
	 *
	 * @param oldDbSchemaVersion schema version of the existing database or 0
	 * @return new Schema Version that will be passed next time the database
	 *         connection will be opened @
	 */
	protected abstract int updateDatabaseSchema(int oldDbSchemaVersion);

	private Connection createConnection() {
		synchronized (connections) {
			final String connectionString = getConnectionString();
			if (connections.containsKey(connectionString) && !"jdbc:h2:mem:".equals(connectionString)) {
				return connections.get(connectionString);
			}
			Connection connection;
			try {
				connection = DriverManager.getConnection(connectionString, getUserName(), getPassword());
			} catch (final SQLException e) {
				throw new DatabaseException(e);
			}
			checkVersion(connection);
			connections.put(connectionString, connection);
			return connection;
		}
	}

	protected Connection getConnection() {
		if (localConnection == null) {
			localConnection = createConnection();
		}
		return localConnection;
	}

	private void checkVersion(final Connection con) {
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
			tempConnection = con;
			dbSchemaVersion = updateDatabaseSchema(dbSchemaVersion);
			tempConnection = null;
			statementForSchemaUpdate = null;
		} catch (final SQLException e) {
			throw new DatabaseException(e);
		}

		try (PreparedStatement preparedStatement = con
				.prepareStatement("UPDATE CustomProperty SET Value = ? WHERE Key = ?")) {
			preparedStatement.setInt(1, dbSchemaVersion);
			preparedStatement.setString(2, "dbShemaVersion");
			preparedStatement.execute();
		} catch (final SQLException e) {
			throw new DatabaseException(e);
		}
	}

	protected void dropTable(final Class<?> cls) {
		dropTable(cls.getSimpleName());
		final List<Field> fields = ReflectionUtil.getAllFields(cls);
		for (final Field field : fields) {
			final String mappedType = MappingHelper.mapDatabaseType(field);
			if (mappedType == null) {
				final String mappingTableName = String.format("Mapping_%s_%s", cls.getSimpleName(), field.getName());
				dropTable(mappingTableName);
			}
		}
	}

	protected void dropTable(final String table) {
		if (statementForSchemaUpdate == null) {
			throw new DatabaseException("dropTable is only allowed during updateDatabaseSchema process");
		}
		final String dropTableQuery = String.format("DROP TABLE %s", table);
		LOGGER.info(dropTableQuery);
		execute(statementForSchemaUpdate, dropTableQuery);
	}

	private static boolean execute(final Statement statement, final String query) {
		try {
			return statement.execute(query);
		} catch (final SQLException e) {
			throw new DatabaseException(e);
		}
	}

	protected void createTable(final Class<?> cls) {
		if (statementForSchemaUpdate == null) {
			throw new DatabaseException("createTable is only allowed during updateDatabaseSchema process");
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
		execute(statementForSchemaUpdate, createTableQuery);
	}

	protected interface UpdateData {
		void execute();
	}

	/**
	 * This method is supposed to allow Data changes during the updateDatabaseSchema
	 * process.
	 *
	 * @param updateData
	 */
	protected void updateData(final UpdateData updateData) {
		if (localConnection != null) {
			throw new DatabaseException("Please don't use updateData() outside of updateDatabaseSchema()");
		}
		localConnection = tempConnection;
		updateData.execute();
		localConnection = null;
	}

	protected void updateTable(final Class<?> cls) {
		updateTable(cls, false);
	}

	protected void updateTable(final Class<?> cls, final boolean allowDeletion) {
		if (statementForSchemaUpdate == null) {
			throw new DatabaseException("createTable is only allowed during updateDatabaseSchema process");
		}
		final Map<String, String> existingFields = analyseExistingFieldsForTable(cls);

		final List<Field> fields = ReflectionUtil.getAllFields(cls);
		for (final Field field : fields) {
			final String mappedType = MappingHelper.mapDatabaseType(field);

			final String fieldName = field.getName().toUpperCase();
			if (!existingFields.containsKey(fieldName)) {
				alterTableAddField(cls, field, mappedType);
				continue;
			}
			final String fieldType = existingFields.get(fieldName);
			if (fieldType.equalsIgnoreCase(mappedType)) {
				existingFields.remove(fieldName);
			} else if (List.class.isAssignableFrom(field.getType()) && "List".equals(fieldType)) {
				existingFields.remove(fieldName);
			} else if ("ENUM".equals(fieldType) && mappedType.startsWith("ENUM")) {
				existingFields.remove(fieldName);
			} else {
				throw new UnsupportedOperationException("Cannot change field type");
			}
		}

		for (final Entry<String, String> entry : existingFields.entrySet()) {
			if (allowDeletion) {
				alterTableDropField(cls, entry);
			} else {
				LOGGER.warn("Warning: Field " + entry.getKey() + " of class " + cls.getSimpleName() + " is unused");
			}
		}

	}

	private Map<String, String> analyseExistingFieldsForTable(final Class<?> cls) {
		final Map<String, String> existingFields = new HashMap<>();
		try (ResultSet resultSetColumns = statementForSchemaUpdate.executeQuery(
				"SELECT COLUMNS.COLUMN_NAME, COLUMNS.TYPE_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE COLUMNS.TABLE_NAME = '"
						+ cls.getSimpleName().toUpperCase() + "'")) {
			while (resultSetColumns.next()) {
				existingFields.put(resultSetColumns.getString(1), resultSetColumns.getString(2));
			}
		} catch (final SQLException e) {
			throw new DatabaseException(e);
		}
		try (ResultSet resultSetMappingTable = statementForSchemaUpdate
				.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLES.TABLE_NAME LIKE 'MAPPING_"
						+ cls.getSimpleName().toUpperCase() + "_%'")) {
			while (resultSetMappingTable.next()) {
				final String tableName = resultSetMappingTable.getString(1)
						.replace("MAPPING_" + cls.getSimpleName().toUpperCase() + "_", "");
				existingFields.put(tableName, "List");
			}
		} catch (final SQLException e) {
			throw new DatabaseException(e);
		}
		return existingFields;
	}

	private void alterTableDropField(final Class<?> cls, final Entry<String, String> entry) {
		final StringBuilder query = new StringBuilder();
		query.append("ALTER TABLE ");
		query.append(cls.getSimpleName());
		query.append(" DROP COLUMN ");
		query.append(entry.getKey());
		query.append(";");
		final String alterTableQuery = query.toString();
		LOGGER.info(alterTableQuery);
		execute(statementForSchemaUpdate, alterTableQuery);
	}

	private void alterTableAddField(final Class<?> cls, final Field field, final String mappedType) {
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
			execute(statementForSchemaUpdate, alterTableQuery);
		} else {
			createMappingTableFor(field, statementForSchemaUpdate);
		}
	}

	private static void createMappingTableFor(final Field field, final Statement statement) {
		final Class<?> fromClazz = field.getDeclaringClass();

		final Class<?> type = ReflectionUtil.getCollectionType(field);

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
		} else if (type.isEnum()) {
			stringBuilder.append(type.getSimpleName());
			stringBuilder.append(" VARCHAR");
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
		execute(statement, createTableQuery);

	}

	/**
	 * Creates a Query for selecting Objects of type clazz.
	 *
	 * @param clazz Class corresponding to a DB Table
	 * @return Fluent style based query builder @ SQLException in case of an error
	 */
	public <T> InitialQueryConnection<T, AbstractStatement<T>> select(final Class<T> clazz) {
		final AbstractStatement<T> statement = SelectStatement.select(clazz, getConnection());
		return new InitialQueryConnection<>(statement, getConnection());
	}

	public <T> UUID insertShallow(final T objectToInsert) {
		return PreparedInsert.insert(objectToInsert, getConnection(), Cascade.NONE);
	}

	/**
	 * Deep insert of the Object and recursively their children.
	 *
	 * @param objectToInsert object that should be persisted
	 * @return id of the inserted object @ SQLException in case of an error
	 */
	public <T> UUID insert(final T objectToInsert) {
		return PreparedInsert.insert(objectToInsert, getConnection(), Cascade.INSERT);
	}

	public <T> int update(final T objectToUpdate) {
		return PreparedUpdate.update(objectToUpdate, getConnection(), Cascade.UPDATE);
	}

	public <T> int updateShallow(final T objectToUpdate) {
		return PreparedUpdate.update(objectToUpdate, getConnection(), Cascade.NONE);
	}

	public <T> UUID merge(final T objectToMerge) {
		return PreparedMerge.merge(objectToMerge, getConnection(), Cascade.MERGE);
	}

	public <T> UUID mergeShallow(final T objectToMerge) {
		return PreparedMerge.merge(objectToMerge, getConnection(), Cascade.NONE);
	}

	/**
	 * Directly deletes a Object and recursively its children.
	 *
	 * @param objectToDelete Object that shall be deleted from Database @
	 *                       SQLException in case of an error
	 */
	public <T> void delete(final T objectToDelete) {
		PreparedDelete.delete(objectToDelete, getConnection(), Cascade.DELETE);
	}

	/**
	 * Deletes a Object. Referenced Objects will not be deleted.
	 *
	 * @param objectToDelete Object that shall be deleted from Database @
	 *                       SQLException in case of an error
	 */
	public <T> void deleteShallow(final T objectToDelete) {
		PreparedDelete.delete(objectToDelete, getConnection(), Cascade.NONE);
	}

	public int executeNative(final String sql, final Object... args) {
		try (final PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
			int i = 1;
			for (final Object object : args) {
				final Object dbObject = MappingHelper.getDbObject(object);
				preparedStatement.setObject(i++, dbObject);
			}
			return preparedStatement.executeUpdate();
		} catch (final SQLException e) {
			throw new DatabaseException(e);
		}
	}
}
