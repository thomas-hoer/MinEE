package de.minee.jpa;

public class OrQueryConnection<T> extends AbstractAndOrConnection<T> {

	public OrQueryConnection(final AbstractStatement<T> statement) {
		super(statement);
	}

	@Override
	protected String getConnectionString() {
		return " OR ";
	}

}
