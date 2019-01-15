package de.minee.jpa;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface IStatement<T> {
	Class<T> getType();

	<U extends IStatement<T>> void add(AbstractAndOrConnection<T, U> abstractAndOrConnection);

	T byId(UUID id) throws SQLException;

	List<T> execute() throws SQLException;

	AbstractStatement<T> query(String manualQuery);
}
