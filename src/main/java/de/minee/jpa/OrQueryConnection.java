package de.minee.jpa;

public class OrQueryConnection<T, U extends IStatement<T>> extends AbstractAndOrConnection<T, U> {

	public OrQueryConnection(final U statement) {
		super(statement);
	}

	@Override
	protected String getConnectionString() {
		return " OR ";
	}

}
