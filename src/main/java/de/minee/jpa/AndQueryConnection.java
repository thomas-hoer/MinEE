package de.minee.jpa;

public class AndQueryConnection<T, U extends IStatement<T>> extends AbstractAndOrConnection<T, U> {

	public AndQueryConnection(final U statement) {
		super(statement);
	}

	@Override
	protected String getConnectionString() {
		return " AND ";
	}

}
