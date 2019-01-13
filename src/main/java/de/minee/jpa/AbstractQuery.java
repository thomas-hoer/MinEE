package de.minee.jpa;

import de.minee.jpa.AbstractPreparedQuery.ResultSetConsumer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractQuery {

	protected abstract Connection getConnection();

	protected static void executeQuery(final PreparedStatement statement, final ResultSetConsumer consumer)
			throws SQLException {
		try (ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				consumer.accept(resultSet);
			}
		}
	}

	protected void executeQuery(final String query, final ResultSetConsumer consumer) throws SQLException {
		try (Statement statement = getConnection().createStatement();
				ResultSet resultSet = statement.executeQuery(query)) {
			while (resultSet.next()) {
				consumer.accept(resultSet);
			}
		}
	}

}
