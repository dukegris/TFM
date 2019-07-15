/*******************************************************************************
 * Copyright (c) 2018 @gt_tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package es.rcs.tfm.db.util;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

/**
 * Interface establishes contract for providing {@link Predicate} or {@link BooleanExpression} to be used to pass to
 * QueryDSL for querying underlying store.
 *
 * <p>
 * This interface and entire component of QueryDSL extension is to build on top of Spring data QueryDSL extensions
 * for QueryDSL by providing further low level of search operators within values which is a powerful extension by
 * empowering client application to perform variety searches, an improvement over largely static capability out of
 * the box to statically define different binding using QuerydslBinderCustomizer.
 * </p>
 *
 *
 * With this extension, client's can request in a variety of forms like below:
 * <ul>
 * <li>Search all resources which has either any email in 'company.com' domain or else have a
 * specific email specified by second parameter
 * /api/user/search?emails.value=endsWith(@company.com)&amp;emails.value=johndoe@somemail.com</li>
 * <li>Search for a user having <b>any <i>*admin*</i></b> role - /api/user/search?role=contains(admin)</li>
 * <li>Search for any user having not having email in some specific domain - /api/user/search?emails
 * .value=not(contains(@company.com))</li>
 * <li>Search any user not having a specific attribute - job_level as "executive"
 * /api/user/search?profile.job_level=ne(executive)</li>
 * </ul>
 *
 *
 *
 * <p>
 * Interface also defines supported operators though specific implementation may only support a subset of them so
 * must be checked on implementation on supported Operators to avoid unpredictable errors in search logic/processing.
 * Implementation must support all Logical Operators.
 * </p>
 * @param <P> type of {@link com.querydsl.core.types.Path}, example - {@link com.querydsl.core.types.dsl.StringPath} or {@link com.querydsl.core.types.dsl.EnumPath}.
 * @param <V> type of value for path. Depending on type of path, this could be a collection of String or else Collection of String or String or Enum or Collection of Enum. This must not be wrapped in Optional.
 * @author gt_tech
 * @see EPOperator
 * @see <a href="https://docs.spring.io/spring-data/mongodb/docs/2.0.6.RELEASE/reference/html/#core.extensions.querydsl">Spring data Querydsl Extensions</a>
 * @see <a href="https://docs.spring.io/spring-data/commons/docs/2.0.6.RELEASE/api/org/springframework/data/querydsl/binding/QuerydslBinderCustomizer.html">QuerydslBinderCustomizer</a>
 */
@SuppressWarnings("rawtypes")
public interface EP<P extends Path, V> {

	/**
	 * Provides a operator value delimiter prefix for when explicit delimiter is provided.
	 * for e.g. operator(value)
	 */
	public static final String OPERATOR_VALUE_DELIMITER_PREFIX = "(";
	
	/**
	 * Provides a operator value delimiter suffix for when explicit delimiter is provided.
	 * for e.g. operator(value)
	 */
	public static final String OPERATOR_VALUE_DELIMITER_SUFFIX = ")";


	/**
	 * Method establishes contract to retrieve a predicate based on implementation specific logic's processing of
	 * supplied value(s).
	 * <p>
	 * Default implementation delegates to {@link #getExpression(Path, Object)}
	 * </p>
	 *
	 * @param path  Q Path <code>Path</code> for which expression is to be formed using supplied <code>value</code>
	 * @param value Input value <code>value(s)</code> to be used to form expression, this can be a primitive or a collection of values but must not be wrapped in {@link Optional}
	 * @return {@link Optional} of {@link Predicate} based on provided value.
	 */
	default Optional<Predicate> getPredicate(P path, V value) {
		return this
			.getExpression(path, value)
			.map(Predicate.class::cast);
	}

	/**
	 * Method establishes the contract to retrieve a {@link BooleanExpression} based on implementation specific
	 * logic's processing of supplied value(s).
	 * <p>
	 * Default implementation returns an empty Optional.
	 * </p>
	 *
	 * @param path  Q Path <code>Path</code> for which expression is to be formed using supplied <code>value</code>
	 * @param value Input value <code>value(s)</code> to be used to form expression, this can be a primitive or a collection of values but must not be wrapped in {@link Optional}
	 * @return {@link Optional} of {@link BooleanExpression} based on provided value.
	 * @throws UnsupportedOperationException if an operator is used in unsupported order or on unsupported digits (for example, startWith operator being used on String values)
	 */
	default Optional<BooleanExpression> getExpression(P path, V value) {
		return Optional.empty();
	}


	/**
	 * Utility function to check if provided value starts with an EPOperator.
	 * Compares against all available Operators.
	 *
	 * @param value input string to check for EPOperator
	 * @return <code>EPOperator</code> if supplied String starts with an operator, <code>empty Optional</code> otherwise
	 * @throws UnsupportedOperationException if an operator is used in unsupported order or on unsupported digits (for example, startWith operator being used on String values)
	 */
	public static <S extends String> Optional<EPOperator> isOperator(final S value) {
		return isOperator(EPOperator.values(), value);
	}

	/**
	 * Utility function to check if provided value starts with an EPOperator.
	 * Compares against provided operators.
	 *
	 * @param value	 input string to check for EPOperator
	 * @param operators List of operators to check against
	 * @return <code>EPOperator</code> if supplied String starts with an operator, <code>empty Optional</code> otherwise
	 * @throws UnsupportedOperationException if an operator is used in unsupported order or on unsupported digits (for example, startWith operator being used on String values)
	 */
	public static <S extends String> Optional<EPOperator> isOperator(EPOperator[] operators, final S value) {
		if (operators != null && StringUtils.isNotBlank(value)) {
			for (EPOperator operator : operators) {
				if (isOperator(operator, value)) {
					return Optional.of(operator);
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Returns <code>true</code> if provided value is wrapped in supplied <code>operator</code>
	 *
	 * @param operator <code>EPOperator</code> to check for on provided value
	 * @param value	<code>value</code> to check against if it's wrapped in provided <code>operator</code>
	 * @return <code>true</code> if provided value is wrapped in supplied <code>operator</code>, <code>false</code> otherwise
	 */
	static boolean isOperator(EPOperator operator, final String value) {
		return 
			value.startsWith(
				new StringBuilder(operator.toString())
					.append(OPERATOR_VALUE_DELIMITER_PREFIX)
					.toString()) && 
			value.endsWith(
					OPERATOR_VALUE_DELIMITER_SUFFIX);
	}

	/**
	 * Utility method which validates proper ordering and opening/closing delimiters of operators on supplied <code>value</code>
	 *
	 * @param value <code>value</code> to check for proper composition
	 * @throws IllegalArgumentException if an invalid composition is found in provided <code>value</code>
	 */
	static void validateComposition(final String value) {
		if (StringUtils.isNotBlank(value)) {
			if (isOperator(value).isPresent()) {
				int count = 0;
				int opening_bracket = OPERATOR_VALUE_DELIMITER_PREFIX.charAt(0);
				int closing_bracket = OPERATOR_VALUE_DELIMITER_SUFFIX.charAt(0);
				for (char c : value.toCharArray()) {
					if (c == opening_bracket)
						count++;
					else if (c == closing_bracket) {
						if (count <= 0) {
							throw new IllegalArgumentException("Malformed (bad-ordering) value: " + value);
						}
						count--;
					}
				}
				if (count != 0) {
					throw new IllegalArgumentException("Malformed (Incompletely closed) value: " + value);
				}
			}
		}
	}
}
