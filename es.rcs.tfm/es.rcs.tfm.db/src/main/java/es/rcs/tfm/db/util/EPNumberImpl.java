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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;

/**
 * Implementation of {@link EPBase} for supporting
 * {@link NumberPath}
 * 
 * @author gt_tech
 *
 */
@SuppressWarnings( {"rawtypes", "unchecked" } )
class EPNumberImpl extends EPBase<NumberPath> {

	public EPNumberImpl() {
		super(Arrays.asList(
		EPOperator.EQUAL, 
		EPOperator.NOT_EQUAL, 
		EPOperator.GREATER_THAN, 
		EPOperator.GREATER_THAN_OR_EQUAL, 
		EPOperator.LESS_THAN,
		EPOperator.NOT, 
		EPOperator.LESS_THAN_OR_EQUAL));
	}

	@Override protected <S extends String> S getStringValue(NumberPath path, Object value) {
		return (S) String.valueOf(value);
	}

	@Override protected BooleanExpression eq(NumberPath path, String value, boolean ignoreCase) {
		Validate.isTrue(StringUtils.isNumeric(value), "Invalid numeric value");
		return path.eq(Integer.parseInt(StringUtils.trim(value)));
	}

	@Override protected BooleanExpression ne(NumberPath path, String value, boolean ignoreCase) {
		Validate.isTrue(StringUtils.isNumeric(value), "Invalid numeric value");
		return path.ne(Integer.parseInt(StringUtils.trim(value)));
	}

	@Override protected BooleanExpression contains(NumberPath path, String value, boolean ignoreCase) {
		throw new UnsupportedOperationException("Number can't be searched using contains operator");
	}

	public static final Pattern NUMBERS_RANGE_PTRN = Pattern.compile("\\s*(\\d+)\\s*?[\\-aA]\\s*(\\d+)\\s*");
	@Override protected BooleanExpression like(NumberPath path, String value, boolean ignoreCase) {
		
		String withoutComillas = value.replaceAll("\"", "");
		String[] strs = withoutComillas.split("\\s*\\,\\s*");

		BooleanExpression predicate = null;
		
		for (String str: strs) {
			BooleanExpression result = null;
			if (StringUtils.isNumeric(str)) {
				result = path.eq(str);
			} else {
				Matcher m = NUMBERS_RANGE_PTRN.matcher(str);
				Integer izq = null;
				Integer der = null;
				if (m.find()) {
					if (m.group(1) != null) {
						try {
							izq = new Integer(m.group(1));
						} catch (NumberFormatException ex) {
						}
					}
					if (m.group(2) != null) {
						try {
							der = new Integer(m.group(2));
						} catch (NumberFormatException ex) {
						}
					}
				}
				if ((izq != null) && (der != null)) {
					result = path.between(izq, der);
				}
			}
			if (result != null) {
				if (predicate == null) {
					predicate = result;
				} else {
					predicate.or(result);
				}
			}
		}
            
		return predicate;
		
	}

	@Override protected BooleanExpression startsWith(NumberPath path, String value, boolean ignoreCase) {
		throw new UnsupportedOperationException("Number can't be searched using startsWith operator");
	}

	@Override protected BooleanExpression endsWith(NumberPath path, String value, boolean ignoreCase) {
		throw new UnsupportedOperationException("Number can't be searched using endsWith operator");
	}

	@Override protected BooleanExpression matches(NumberPath path, String value) {
		throw new UnsupportedOperationException("Number can't be searched using matches operator");
	}

	@Override protected BooleanExpression gt(NumberPath path, String value) {
		Validate.isTrue(StringUtils.isNumeric(value), "Invalid numeric value");
		return path.gt(Integer.parseInt(StringUtils.trim(value)));
	}

	@Override protected BooleanExpression gte(NumberPath path, String value) {
		Validate.isTrue(StringUtils.isNumeric(value), "Invalid numeric value");
		return path.goe(Integer.parseInt(StringUtils.trim(value)));
	}

	@Override protected BooleanExpression lt(NumberPath path, String value) {
		Validate.isTrue(StringUtils.isNumeric(value), "Invalid numeric value");
		return path.lt(Integer.parseInt(StringUtils.trim(value)));
	}

	@Override protected BooleanExpression lte(NumberPath path, String value) {
		Validate.isTrue(StringUtils.isNumeric(value), "Invalid numeric value");
		return path.loe(Integer.parseInt(StringUtils.trim(value)));
	}
}
