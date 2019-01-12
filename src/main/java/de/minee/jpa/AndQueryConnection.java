package de.minee.jpa;

public class AndQueryConnection<T> extends AbstractAndOrConnection<T> {

	public AndQueryConnection(final AbstractStatement<T> statement) {
		super(statement);
	}

	@Override
	protected String getConnectionString() {
		return " AND ";
	}

}
