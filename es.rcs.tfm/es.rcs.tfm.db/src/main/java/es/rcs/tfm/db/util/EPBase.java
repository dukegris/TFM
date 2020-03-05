/*******************************************************************************
 * Copyright (c) 2018 @gt_tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package es.rcs.tfm.db.util;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;

/**
 * Base implementation of {@link EP} which handles the plumbing
 * of extracting operators with their composition and delegates to specific
 * implementation for final expression formation.
 * <p>
 * <p>
 * Implementation must support a specific implementation of {@link Path} type
 * and may chose to not support every {@link EPOperator} depending on it's own
 * logic. In which case implementation is required to throw an
 * {@link UnsupportedOperationException}
 * </p>
 *
 * @param <P> type of {@link Path}
 * @author gt_tech
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
abstract class EPBase<P extends Path> implements EP<P, Object> {

	private final List<EPOperator> SUPPORTED_SINGLE_VALUED_COMPARISON_OPERATORS;

	/**
	 * Constructor
	 *
	 * @param supportedSingleValueComparisonOperators Collection of {@link EPOperator} 
	 * supported by specific implementation of this base class for single-value comparisons.
	 */
	public EPBase(List<EPOperator> supportedSingleValueComparisonOperators) {
		Validate.isTrue(CollectionUtils.isNotEmpty(supportedSingleValueComparisonOperators),
				"Supported Single value" + " operators must be > 1");
		this.SUPPORTED_SINGLE_VALUED_COMPARISON_OPERATORS = supportedSingleValueComparisonOperators;
	}

	@Override
	public Optional<BooleanExpression> getExpression(P path, Object value) {
		return Optional.ofNullable(path) // check path
			.map(p -> value) // check for value
			.map(v -> {
				if (Collection.class.isAssignableFrom(v.getClass())) {
					return new MultiValueExpressionBuilder(path, (Collection) v).getExpression();
				} else {
					/*
					 * delegate to MultiValueExpressionBuilder instead of
					 * SingleValueExpressionBuilder as MultiValueExpressionBuilder also checks for
					 * QuerydslHttpRequestContext if its needed. SingleValueExpressionBuilder
					 * doesn't do it.
					 */
					return new MultiValueExpressionBuilder(path, Arrays.asList(getStringValue(path, v)))
							.getExpression();
				}
			});

	}

	/*
	 * START: Methods for concrete implementation in sub-classes depending on if
	 * Path sub-type doesn't support same logic and may require a sub-query
	 * expression.
	 */

	/**
	 * Returns String value for provided object (value supplied by bindings during
	 * bindings invocation phase)
	 *
	 * @param path  Specific type of {@link Path}
	 * @param value Value as received from bindings invoker.
	 * @return String value for the provided value object.
	 */
	protected abstract <S extends String> S getStringValue(P path, Object value);

	/**
	 * Creates a expression for equals clause - {@link EPOperator#EQUAL} operator
	 *
	 * @param path  Specific type of {@link Path}
	 * @param value String value to be used for making expression.
	 * @param ignoreCase if comparison must be done ignoring case if case is applicable to target value type.
	 * @return {@link BooleanExpression} to be used further by downstream query serialization logic for executing actual query
	 * @throws UnsupportedOperationException if implementation doesn't support this {@link EPOperator}
	 */
	protected abstract BooleanExpression eq(P path, String value, boolean ignoreCase);

	/**
	 * Creates a expression for not-equals clause - {@link EPOperator#NOT_EQUAL}
	 * operator
	 *
	 * @param path  Specific type of {@link Path}
	 * @param value String value to be used for making expression.
	 * @param ignoreCase if comparison must be done ignoring case if case is applicable to target value type.
	 * @return {@link BooleanExpression} to be used further by downstream query serialization logic for executing actual query
	 * @throws UnsupportedOperationException if implementation doesn't support this {@link EPOperator}
	 */
	protected abstract BooleanExpression ne(P path, String value, boolean ignoreCase);

	/**
	 * Creates a expression for contains clause - {@link EPOperator#CONTAINS}
	 * operator
	 *
	 * @param path  Specific type of {@link Path}
	 * @param value String value to be used for making expression
	 * @param ignoreCase if comparison must be done ignoring case if case is  applicable to target value type. .
	 * @return {@link BooleanExpression} to be used further by downstream query serialization logic for executing actual query
	 * @throws UnsupportedOperationException if implementation doesn't support this {@link EPOperator}
	 */
	protected abstract BooleanExpression contains(P path, String value, boolean ignoreCase);

	/**
	 * Creates a expression for like clause - {@link EPOperator#LIKE}
	 * operator
	 *
	 * @param path  Specific type of {@link Path}
	 * @param value String value to be used for making expression
	 * @param ignoreCase if comparison must be done ignoring case if case is  applicable to target value type. .
	 * @return {@link BooleanExpression} to be used further by downstream query serialization logic for executing actual query
	 * @throws UnsupportedOperationException if implementation doesn't support this {@link EPOperator}
	 */
	protected abstract BooleanExpression like(P path, String value, boolean ignoreCase);

	/**
	 * Creates a expression for startsWith clause - {@link EPOperator#STARTS_WITH}
	 * operator
	 *
	 * @param path  Specific type of {@link Path}
	 * @param value String value to be used for making expression.
	 * @param ignoreCase if comparison must be done ignoring case if case is applicable to target value type.
	 * @return {@link BooleanExpression} to be used further by downstream query serialization logic for executing actual query
	 * @throws UnsupportedOperationException if implementation doesn't support this {@link EPOperator}
	 */
	protected abstract BooleanExpression startsWith(P path, String value, boolean ignoreCase);

	/**
	 * Creates a expression for endsWith clause - {@link EPOperator#ENDS_WITH}
	 * operator
	 *
	 * @param path  Specific type of {@link Path}
	 * @param value String value to be used for making expression.
	 * @param ignoreCase if comparison must be done ignoring case if case is applicable to target value type.
	 * @return {@link BooleanExpression} to be used further by downstream query serialization logic for executing actual query
	 * @throws UnsupportedOperationException if implementation doesn't support this {@link EPOperator}
	 */
	protected abstract BooleanExpression endsWith(P path, String value, boolean ignoreCase);

	/**
	 * Creates a expression for matches clause - {@link EPOperator#MATCHES} operator
	 *
	 * @param path  Specific type of {@link Path}
	 * @param value String value to be used for making expression.
	 * @return {@link BooleanExpression} to be used further by downstream query serialization logic for executing actual query
	 * @throws UnsupportedOperationException if implementation doesn't support this {@link EPOperator}
	 */
	protected abstract BooleanExpression matches(P path, String value);

	/**
	 * Creates a expression for greater-than clause - {@link EPOperator#GREATER_THAN}
	 * operator
	 *
	 * @param path  Specific type of {@link Path}
	 * @param value String value to be used for making expression.
	 * @return {@link BooleanExpression} to be used further by downstream query serialization logic for executing actual query
	 * @throws UnsupportedOperationException if implementation doesn't support this {@link EPOperator}
	 */
	protected abstract BooleanExpression gt(P path, String value);

	/**
	 * Creates a expression for greater-than-equal clause -
	 * {@link EPOperator#GREATER_THAN_OR_EQUAL} operator
	 *
	 * @param path  Specific type of {@link Path}
	 * @param value String value to be used for making expression.
	 * @return {@link BooleanExpression} to be used further by downstream query serialization logic for executing actual query
	 * @throws UnsupportedOperationException if implementation doesn't support this {@link EPOperator}
	 */
	protected abstract BooleanExpression gte(P path, String value);

	/**
	 * Creates a expression for less-than clause - {@link EPOperator#LESS_THAN}
	 * operator
	 *
	 * @param path  Specific type of {@link Path}
	 * @param value String value to be used for making expression.
	 * @return {@link BooleanExpression} to be used further by downstream query serialization logic for executing actual query
	 * @throws UnsupportedOperationException if implementation doesn't support this {@link EPOperator}
	 */
	protected abstract BooleanExpression lt(P path, String value);

	/**
	 * Creates a expression for less-than or equal clause -
	 * {@link EPOperator#LESS_THAN_OR_EQUAL} operator
	 *
	 * @param path  Specific type of {@link Path}
	 * @param value String value to be used for making expression.
	 * @return {@link BooleanExpression} to be used further by downstream query serialization logic for executing actual query
	 * @throws UnsupportedOperationException if implementation doesn't support this {@link EPOperator}
	 */
	protected abstract BooleanExpression lte(P path, String value);

	/*
	 * STOP: Abstract methods for concrete implementation
	 */

	/**
	 * Logical operators implementation
	 */
	/**
	 * Applies a logical NOT (negate) to provided expression.
	 *
	 * @param  expression
	 * @return Negated expression that must be used further in expression-building process
	 */
	protected final BooleanExpression not(BooleanExpression expression) {
		Validate.notNull(expression);
		return expression.not();
	}

	/**
	 * Applies logical AND clause to provided expressions.
	 *
	 * @param left  Left operand for AND operation
	 * @param right Right operand for AND operation
	 * @return expression with AND clause applied to provided two values, this must be used further in expression-building process
	 */
	protected final BooleanExpression and(BooleanExpression left, BooleanExpression right) {
		Validate.notNull(left);
		Validate.notNull(right);
		return left.and(right);
	}

	/**
	 * Applies logical OR clause to provided expressions.
	 *
	 * @param left  Left operand for OR operation
	 * @param right Right operand for OR operation
	 * @return expression with AND clause applied to provided two values, this must be used further in expression-building process
	 */
	protected final BooleanExpression or(BooleanExpression left, BooleanExpression right) {
		Validate.notNull(left);
		Validate.notNull(right);
		return left.or(right);
	}

	/**
	 * Utility class for building stateful expressions from provided values
	 */
	private class MultiValueExpressionBuilder {

		private final P path;
		private final Collection<Object> values;
		private BooleanExpression expression;

		private final Collection<EPOperator> MULTI_VALUE_LOGICAL_OPERATORS = 
			Collections
				.unmodifiableCollection(Arrays.asList(
						EPOperator.AND, 
						EPOperator.OR));

		public MultiValueExpressionBuilder(P path, Collection<Object> values) {
			this.path = path;
			this.values = values;
			this.values.forEach(v -> EP.validateComposition(getStringValue(path, v)));
		}

		public BooleanExpression getExpression() {

			EPOperator default_operator = null; 
			// if first param overrides the default for multi-value to be AND, we set it here, 
			// this would help that if first EPOperator for a multi-value comparison has and(..) 
			// then all subsequent value will use and(..) as default operator 
			// instead of default OR
			if (CollectionUtils.isNotEmpty(this.values)) {
				if (this.values.size() == 1) {
					String value = checkIfOriginalRequestValueAvailable(
							path,
							getStringValue(path, this.values.iterator().next()));
					while (true) {
						if (EP
								.isOperator(
									MULTI_VALUE_LOGICAL_OPERATORS.toArray(new EPOperator[MULTI_VALUE_LOGICAL_OPERATORS.size()]), 
									value)
								.isPresent()) {
							// got an ill-placed Logical operator that's meant
							// for multi-value searches on fields
							value = new EPOperatorAndValue(
									value, 
									MULTI_VALUE_LOGICAL_OPERATORS, EPOperator.OR)
											.getValue();
						} else {
							// got true value devoid of multi-value logical search operators.
							break;
						}
					}
					/*
					 * Strip any ill-placed logical operator
					 */
					return new SingleValueExpressionBuilder(path, value).getExpression();
				} else {
					for (Object o : checkIfOriginalRequestValuesAvailable(path, values)
							.stream()
							.filter(Objects::nonNull)
							.collect(Collectors.toList())) {
						final String v = getStringValue(this.path, o);
						EPOperatorAndValue ov = new EPOperatorAndValue(
								v, 
								MULTI_VALUE_LOGICAL_OPERATORS,
								default_operator != null ? default_operator : EPOperator.OR);
						if (default_operator == null && !EPOperator.NOT.equals(ov.getOperator()))
							default_operator = ov.getOperator();
						/*
						 * For NOT. delegate the comparison to SingleValueExpressionBuilder. Known issue
						 * with NOT operator with multiple leafs within MongoDB seralizer, issue opened
						 * on its JIRA site by self.
						 */
						final SingleValueExpressionBuilder e = new SingleValueExpressionBuilder(
								path,
								EPOperator.NOT.equals(ov.getOperator()) ? v : ov.getValue());
						BooleanExpression current = e.getExpression();
						if (current == null) {
							continue;
						}
						if (expression == null) {
							expression = current;
						} else {
							// compose
							switch (ov.getOperator()) {
							case AND:
								expression = and(expression, current);
								break;
							case OR:
							case NOT: 
								// actual NOT clause is handled by
								// SingleValueExpressionBuilder, at this
								// level we are treating it as like starting
								// without OR, AND and thus defaulting
								// to OR
								expression = or(expression, current);
								break;
							default:
								String msg = MessageFormat.format(
										"Illegal operator: {0}, Search Parameter: " + "{1}, Value: {2}",
										new Object[] { ov.getOperator().toString(), path.toString(), v });
								throw new IllegalArgumentException(msg);
							}
						}
					}
				}

			}
			return expression;
		}
	}

	/**
	 * Utility class for building stateful expression from provided Single value.
	 * This class expects the actual value w/ operators (So if a value has to be
	 * exchanged from {@link QuerydslHttpRequestContext} it must be done prior to
	 * invoking this class)
	 */
	private class SingleValueExpressionBuilder {
		private P path;
		private String value;
		private EPOperator operator;
		private SingleValueExpressionBuilder parent;
		private SingleValueExpressionBuilder next;
		private boolean ignoreCase = false;

		public SingleValueExpressionBuilder(P path, String value) {
			init(path, value);
		}

		private SingleValueExpressionBuilder(final P path, String value, final SingleValueExpressionBuilder parent) {
			this.parent = parent;
			init(path, value);
		}

		/**
		 * @return if case should be ignored.
		 */
		public boolean isIgnoreCase() {
			return ignoreCase;
		}

		/**
		 * @param ignoreCase set <code>true</code> if case sensitivity should be ignored.
		 */
		public void setIgnoreCase(boolean ignoreCase) {
			this.ignoreCase = ignoreCase;
		}

		/*
		 * Extract the operator (or operator chain) and form chained expression builder
		 * from this class.
		 */
		private void init(P path, String value) {
			this.path = path;

			EPOperatorAndValue ov = new EPOperatorAndValue(
					value, 
					SUPPORTED_SINGLE_VALUED_COMPARISON_OPERATORS,
					EPOperator.EQUAL);
			this.operator = ov.getOperator();
			this.value = ov.getValue();
			Validate.notNull(this.operator, "EPOperator must not be null");
			if (EPOperator.NOT.equals(this.operator)) {
				Validate.isTrue(StringUtils.isNotEmpty(this.value), "Sub-operation must be available with NOT operator");
				this.next = new SingleValueExpressionBuilder(path, ov.getValue(), this);
			} else if (EPOperator.CASE_IGNORE.equals(this.operator)) {
				Validate.isTrue(StringUtils.isNotEmpty(this.value),
						"Sub-operation must be available with CASE_IGNORE operator");
				this.next = new SingleValueExpressionBuilder(path, ov.getValue(), this);
				this.next.setIgnoreCase(true);
			} else if (EP
					.isOperator(
							SUPPORTED_SINGLE_VALUED_COMPARISON_OPERATORS
								.toArray(new EPOperator[SUPPORTED_SINGLE_VALUED_COMPARISON_OPERATORS.size()]), 
							this.value)
					.isPresent()) { // TODO: Perhaps check for an unsupported operator here and throw an error
				this.next = new SingleValueExpressionBuilder(path, ov.getValue(), this);
			}

			// check for misplaced boolean operators, they should always be
			// top/first operator but can't be composed within other top level
			// operators.
			if (this.parent != null) {
				Validate.isTrue(!(
						EPOperator.AND.equals(this.operator) || 
						EPOperator.OR.equals(this.operator) || (
								EPOperator.NOT.equals(this.operator) && 
								!EPOperator.NOT.equals(this.parent.operator))),
						"Boolean operators cannot be composed within other operators"); // last
				// expression
				// allows
				// for composition of NOT under NOT though it's useless but
				// technically doesn't hurt.
			}
		}

		public BooleanExpression getExpression() {
			BooleanExpression result;

			switch (this.operator) {
			case CASE_IGNORE:
				result = this.next.getExpression();
				break;
			case EQUAL:
				result = eq(path, this.value, this.isIgnoreCase());
				break;
			case NOT_EQUAL:
				result = ne(path, this.value, this.isIgnoreCase());
				break;
			case CONTAINS:
				result = contains(path, this.value, this.isIgnoreCase());
				break;
			case LIKE:
				result = like(path, this.value, this.isIgnoreCase());
				break;
			case STARTS_WITH:
				result = startsWith(path, this.value, this.isIgnoreCase());
				break;
			case ENDS_WITH:
				result = endsWith(path, this.value, this.isIgnoreCase());
				break;
			case MATCHES:
				result = matches(path, this.value);
				break;
			case NOT:
				result = this.next.getExpression();
				if (result != null) {
					result = result.not();
				}
				break;
			case LESS_THAN:
				result = lt(path, this.value);
				break;
			case LESS_THAN_OR_EQUAL:
				result = lte(path, this.value);
				break;
			case GREATER_THAN:
				result = gt(path, this.value);
				break;
			case GREATER_THAN_OR_EQUAL:
				result = gte(path, this.value);
				break;
			default:
				result = null;

			}
			return result;
		}
	}

	/*
	 * Utility method that attempts to get original search input value for specific
	 * path from QuerydslHttpRequestContext if available for cases when experimental
	 * features using QuerydslHttpRequestContextAwareServletFilter is turned on.
	 */
	private String checkIfOriginalRequestValueAvailable(Path path, String defaultValue) {
		String result = defaultValue;
		/*
		QuerydslHttpRequestContext ctx = QuerydslHttpRequestContextHolder.getContext();
		String result = null;

		if (ctx != null) {
			result = ctx.getSingleValue(path);
		}

		if (StringUtils.isBlank(result)) {
			result = defaultValue;
		}
		*/
		return result;
	}

	/*
	 * Utility method that attempts to get original search input value for specific
	 * path from QuerydslHttpRequestContext if available for cases when experimental
	 * features using QuerydslHttpRequestContextAwareServletFilter is turned on.
	 */
	private Collection<Object> checkIfOriginalRequestValuesAvailable(Path path, Collection<Object> defaultValues) {
		Collection<Object> result = defaultValues;
		/*
		QuerydslHttpRequestContext ctx = QuerydslHttpRequestContextHolder.getContext();
		Collection<Object> result = null;

		if (ctx != null) {

			result = Arrays.stream(Optional.ofNullable(ctx.getAllValues(path)).orElseGet(() -> new String[] {}))
					.map(val -> (Object) val).collect(Collectors.toCollection(LinkedList::new));
		}

		if (CollectionUtils.isEmpty(result)) {
			result = defaultValues;
		}
		*/
		return result;
	}

}