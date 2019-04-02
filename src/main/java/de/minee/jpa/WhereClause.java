package de.minee.jpa;

import de.minee.util.Assertions;
import de.minee.util.ProxyFactory;
import de.minee.util.ProxyFactory.ProxyException;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Function;

public class WhereClause<S, T, U extends AbstractStatement<T>> {

	private final U selectStatement;

	private boolean conditionSet;
	private boolean conditionForPrepare;
	private Operator operator;
	private String condition;
	private final String fieldName;
	private final String joinClause;

	/**
	 * General Class for holding the "Where" information of any SQL Statement
	 *
	 * @param whereField Getter to the Field that is selected
	 * @param statement  Statement on which the Where clause is attached to
	 * @throws SQLException In case no Proxy Object can be created for the Table
	 *                      Class
	 */
	public WhereClause(final Function<T, S> whereField, final U statement) throws SQLException {
		Assertions.assertNotNull(statement);
		Assertions.assertNotNull(whereField);

		this.selectStatement = statement;

		final Class<T> type = statement.getType();
		T proxy;
		try {
			proxy = ProxyFactory.getProxy(type);
		} catch (final ProxyException e) {
			throw new SQLException("Can not create Proxy Object for Type " + type.getSimpleName(), e);
		}
		whereField.apply(proxy);
		final String proxyFieldName = proxy.toString();
		final Field field = ReflectionUtil.getDeclaredField(type, proxyFieldName);
		if (field != null && List.class.isAssignableFrom(field.getType())) {
			final String typeSimpleName = type.getSimpleName();
			final String joinTable = "Mapping_" + typeSimpleName + "_" + proxyFieldName;
			joinClause = " JOIN " + joinTable + " ON " + joinTable + "." + typeSimpleName + " = " + typeSimpleName
					+ ".id ";
			final ParameterizedType mapToType = (ParameterizedType) field.getGenericType();
			final Class<?> paramType = (Class<?>) mapToType.getActualTypeArguments()[0];
			fieldName = joinTable + "." + paramType.getSimpleName();
		} else {
			joinClause = "";
			fieldName = type.getSimpleName() + "." + proxyFieldName;
		}
	}

	/**
	 *
	 * @return
	 */
	public U is() {
		checkConditionSet();
		conditionForPrepare = true;
		setCondition(Operator.EQUALS);
		return selectStatement;
	}

	/**
	 *
	 * @param isEqual
	 * @return
	 */
	public U is(final S isEqual) {
		checkConditionSet();
		if (isEqual instanceof List) {
			setJoinCondition((List<?>) isEqual);
		} else {
			setCondition(Operator.EQUALS, isEqual);
		}
		return selectStatement;
	}

	/**
	 *
	 * @param inElements
	 * @return
	 */
	public U in(final S... inElements) {
		checkConditionSet();
		setCondition(Operator.IN, inElements);
		return selectStatement;
	}

	/**
	 *
	 * @return
	 */
	public U isNull() {
		checkConditionSet();
		setCondition(Operator.IS_NULL);
		return selectStatement;
	}

	private static String computeConditionValue(final Object condition) {
		final UUID id = MappingHelper.getId(condition);
		return id != null ? id.toString() : condition.toString();
	}

	private void setJoinCondition(final List<?> list) {
		conditionSet = true;

		if (list.size() == 1) {
			this.operator = Operator.EQUALS;
			this.condition = "'" + computeConditionValue(list.get(0)) + "'";
		} else {
			this.operator = Operator.IN;
			final StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("(");
			final StringJoiner stringJoiner = new StringJoiner(",");
			for (final Object o : list) {
				stringJoiner.add("'" + computeConditionValue(o) + "'");
			}
			stringBuilder.append(stringJoiner.toString());
			stringBuilder.append(")");
			this.condition = stringBuilder.toString();
		}
	}

	private void setCondition(final Operator operator) {
		conditionSet = true;
		this.operator = operator;
		this.condition = "";
	}

	private void setCondition(final Operator operator, final S condition) {
		conditionSet = true;
		this.operator = operator;
		if (condition.getClass().isPrimitive()) {
			this.condition = condition.toString();
		} else {
			this.condition = "'" + computeConditionValue(condition) + "'";
		}
	}

	private void setCondition(final Operator operator, final S[] conditions) {
		conditionSet = true;
		this.operator = operator;
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("(");
		final StringJoiner stringJoiner = new StringJoiner(",");
		for (final S conditionElement : conditions) {
			Assertions.assertNotNull(conditionElement);
			if (conditionElement.getClass().isPrimitive()) {
				stringJoiner.add(conditionElement.toString());
			} else {
				stringJoiner.add("'" + computeConditionValue(conditionElement) + "'");
			}
		}
		stringBuilder.append(stringJoiner.toString());
		stringBuilder.append(")");
		this.condition = stringBuilder.toString();
	}

	private void checkConditionSet() {
		if (conditionSet) {
			throw new MappingException("Condition " + this.toString()
					+ " already set. Multiple operators are not supported. Use .and() or .or() instead.");
		}
	}

	private String getSqlOperator() {
		return operator.getSqlOperation();
	}

	private String getEvaluationValue() {
		return (conditionSet && !conditionForPrepare) ? condition : "?";
	}

	public String getJoinClause() {
		return joinClause;
	}

	@Override
	public String toString() {
		return fieldName + getSqlOperator() + getEvaluationValue();
	}

	private enum Operator {
		EQUALS(" = "), IN(" IN "), IS_NULL(" IS NULL ");

		private String sqlOperation;

		Operator(final String value) {
			sqlOperation = value;
		}

		private String getSqlOperation() {
			return sqlOperation;
		}
	}

}
