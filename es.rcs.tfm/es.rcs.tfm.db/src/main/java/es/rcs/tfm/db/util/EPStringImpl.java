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

import java.util.Arrays;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;

/**
 * Implementation of {@link EPBase} for supporting
 * {@link StringPath}
 * 
 * @author gt_tech
 *
 */
@SuppressWarnings( { "unchecked" } )
class EPStringImpl extends EPBase<StringPath> {

	public EPStringImpl() {
		super(Arrays.asList(
			EPOperator.EQUAL, 
			EPOperator.NOT_EQUAL, 
			EPOperator.CONTAINS, 
			EPOperator.LIKE, 
			EPOperator.STARTS_WITH,
			EPOperator.ENDS_WITH, 
			EPOperator.NOT,
			EPOperator.MATCHES, 
			EPOperator.CASE_IGNORE));
	}

	@Override
	protected <S extends String> S getStringValue(StringPath path, Object value) {
		return (S) value.toString();
	}

	@Override
	protected BooleanExpression eq(StringPath path, String value, boolean ignoreCase) {
		return ignoreCase ? path.equalsIgnoreCase(value): path.eq(value);
	}

	@Override
	protected BooleanExpression ne(StringPath path, String value, boolean ignoreCase) {
		return ignoreCase ? path.notEqualsIgnoreCase(value): path.ne(value);
	}

	@Override
	protected BooleanExpression contains(StringPath path, String value, boolean ignoreCase) {
		return ignoreCase ? path.containsIgnoreCase(value) : path.contains(value);
	}

	@Override
	protected BooleanExpression like(StringPath path, String value, boolean ignoreCase) {
		
		String withoutComillas = value.replaceAll("\"", "");
		String[] strs = withoutComillas.split("\\s*\\,\\s*");

		BooleanExpression predicate = null;
		
		for (String str: strs) {
			str = "%" + str.trim().replaceAll(" ", "%") + "%";
			if (predicate == null) {
				predicate = ignoreCase ? path.likeIgnoreCase(str) : path.like(str);
			} else {
				predicate = predicate.or(ignoreCase ? path.likeIgnoreCase(str) : path.like(str));
			}
		}
            
		return predicate;
		
	}

	@Override
	protected BooleanExpression startsWith(StringPath path, String value, boolean ignoreCase) {
		return ignoreCase ? path.startsWithIgnoreCase(value) : path.startsWith(value);
	}

	@Override
	protected BooleanExpression endsWith(StringPath path, String value, boolean ignoreCase) {
		return ignoreCase ? path.endsWithIgnoreCase(value) : path.endsWith(value);
	}

	@Override
	protected BooleanExpression matches(StringPath path, String value) {
		return path.matches(value);
	}

	@Override
	protected BooleanExpression gt(StringPath path, String value) {
		throw new UnsupportedOperationException("String value can't be searched using gt operator");
	}

	@Override
	protected BooleanExpression gte(StringPath path, String value) {
		throw new UnsupportedOperationException("String value can't be searched using gte operator");
	}

	@Override
	protected BooleanExpression lt(StringPath path, String value) {
		throw new UnsupportedOperationException("String value can't be searched using lt operator");
	}

	@Override
	protected BooleanExpression lte(StringPath path, String value) {
		throw new UnsupportedOperationException("String value can't be searched using lte operator");
	}
}
