package de.minee.jpa;

import java.sql.Connection;

public class ForwardJoinClause<S, T> extends AbstractJoinClause<S, T> {

	private final String fieldName;

	ForwardJoinClause(final InitialQueryConnection<T, ?> queryConnectio, final Class<S> cls, final String fieldName,
			final Connection connection) {
		super(queryConnectio, cls, connection);
		this.fieldName = fieldName;
	}

	@Override
	protected String assembleQuery() {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("JOIN ");
		stringBuilder.append(getType().getSimpleName());
		stringBuilder.append(" ON ");
		stringBuilder.append(getType().getSimpleName());
		stringBuilder.append(".id");
		stringBuilder.append(" = ");
		stringBuilder.append(getQueryConnection().getStatement().getType().getSimpleName());
		stringBuilder.append(".");
		stringBuilder.append(fieldName);
		stringBuilder.append(" ");

		stringBuilder.append(super.assembleQuery());
		return stringBuilder.toString();
	}
}
