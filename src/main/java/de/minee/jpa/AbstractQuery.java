package de.minee.jpa;

import de.minee.jpa.AbstractPreparedQuery.ResultSetConsumer;
import de.minee.util.Assertions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractQuery {

	private final Connection connection;

	protected AbstractQuery(final Connection connection) {
		this.connection = connection;
	}

	protected Connection getConnection() {
		Assertions.assertNotNull(connection, "Database connection cannot be null");
		return connection;
	}

	protected static void executeQuery(final PreparedStatement statement, final ResultSetConsumer consumer) {
		try (ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				consumer.accept(resultSet);
			}
		} catch (final SQLException e) {
			throw new DatabaseException(e);
		}
	}

	protected void executeQuery(final String query, final ResultSetConsumer consumer) {
		try (Statement statement = getConnection().createStatement();
				ResultSet resultSet = statement.executeQuery(query)) {
			while (resultSet.next()) {
				consumer.accept(resultSet);
			}
		} catch (final SQLException e) {
			throw new DatabaseException(e);
		}
	}

}
