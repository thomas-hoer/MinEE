package de.minee.jpa;

import de.minee.util.Assertions;
import de.minee.util.ProxyFactory;
import de.minee.util.ProxyFactory.ProxyException;
import de.minee.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
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
	 * General Class for holding the "Where" information of any SQL Statement.
	 *
	 * @param whereField Getter to the Field that is selected
	 * @param statement  Statement on which the Where clause is attached to
	 */
	public WhereClause(final Function<T, S> whereField, final U statement) {
		Assertions.assertNotNull(statement, "Statement should not be null");
		Assertions.assertNotNull(whereField, "Where field should not be null");

		this.selectStatement = statement;

		final Class<T> type = statement.getType();
		T proxy;
		try {
			proxy = ProxyFactory.getProxy(type);
		} catch (final ProxyException e) {
			throw new DatabaseException("Can not create Proxy Object for Type " + type.getSimpleName(), e);
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
	 * Checks the property to be equal to given value. This creates a prepared
	 * Statement. The value will be passed in the .execute method.
	 *
	 * @return Statement for next condition or execution
	 */
	public U is() {
		checkConditionSet();
		conditionForPrepare = true;
		setCondition(Operator.EQUALS);
		return selectStatement;
	}

	/**
	 * Checks the property to not be equal to isNot.
	 *
	 * @param isNot
	 * @return Statement for next condition or execution
	 */
	public U isNot(final S isNot) {
		checkConditionSet();
		setCondition(Operator.IS_NOT, isNot);
		return selectStatement;
	}

	/**
	 * Checks the property to be equal to isEqual.
	 *
	 * @param isEqual
	 * @return Statement for next condition or execution
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
	 * Checks the property to be one of the specified values of inElements.
	 *
	 * @param inElements Array of Elements that are possible matches
	 * @return Statement for next condition or execution
	 */
	public U in(final S... inElements) {
		checkConditionSet();
		setCondition(Operator.IN, inElements);
		return selectStatement;
	}

	/**
	 * Checks the property to be null.
	 *
	 * @return Statement for next condition or execution
	 */
	public U isNull() {
		checkConditionSet();
		setCondition(Operator.IS_NULL);
		return selectStatement;
	}

	public U gt(final S gt) {
		checkConditionSet();
		setCondition(Operator.GT, gt);
		return selectStatement;
	}

	public U gt() {
		checkConditionSet();
		conditionForPrepare = true;
		setCondition(Operator.GT);
		return selectStatement;
	}

	public U lt(final S lt) {
		checkConditionSet();
		setCondition(Operator.LT, lt);
		return selectStatement;
	}

	public U lt() {
		checkConditionSet();
		conditionForPrepare = true;
		setCondition(Operator.LT);
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
			Assertions.assertNotNull(conditionElement, "One or more condition in the conditionset is null");
			if (conditionElement.getClass().isPrimitive()) {
				stringJoiner.add(conditionElement.toString());
			} else if (Collection.class.isInstance(conditionElement)) {
				final Collection<?> collection = (Collection<?>) conditionElement;
				for (final Object o : collection) {
					if (o.getClass().isPrimitive()) {
						stringJoiner.add(o.toString());
					} else {
						stringJoiner.add("'" + computeConditionValue(o) + "'");
					}
				}
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
		EQUALS(" = "), IN(" IN "), IS_NULL(" IS NULL "), IS_NOT(" != "), GT(" > "), LT(" < ");

		private String sqlOperation;

		Operator(final String value) {
			sqlOperation = value;
		}

		private String getSqlOperation() {
			return sqlOperation;
		}
	}

}
