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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;

/**
 * Implementation of {@link EPBase} for supporting {@link EnumPath}
 *
 * @author gt_tech
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
class EPEnumImpl extends EPBase<EnumPath> {

	final LoadingCache<String, Pattern> regex_pattern_cache = CacheBuilder.newBuilder()
		.maximumSize(500)
		.build(new CacheLoader<String, Pattern>() {
			public Pattern load(String key) { 
				// no checked
				// exception
				return Pattern.compile(key, Pattern.CASE_INSENSITIVE);
			}
		});

	EPEnumImpl() {
		super(EPFactory.isSupportsUnTypedValues()
			? Arrays.asList(
				EPOperator.EQUAL, 
				EPOperator.NOT_EQUAL,
				EPOperator.CONTAINS,
				EPOperator.LIKE,
				EPOperator.STARTS_WITH, 
				EPOperator.ENDS_WITH,
				EPOperator.NOT, 
				EPOperator.MATCHES,
				EPOperator.CASE_IGNORE)
			: Arrays.asList(new EPOperator[]{
				EPOperator.EQUAL,
				EPOperator.NOT_EQUAL,
				EPOperator.NOT
			}));
	}

	@Override protected <S extends String> S getStringValue(EnumPath path, Object value) {
		if (Enum.class.isAssignableFrom(value.getClass())) {
			return (S) ((Enum) value).name();
		}
		return (S) value.toString();
	}

	@Override protected BooleanExpression eq(EnumPath path, String value, boolean ignoreCase) {
		return path.eq(value);
	}

	@Override protected BooleanExpression ne(EnumPath path, String value, boolean ignoreCase) {
		return path.ne(value);
	}

	@Override protected BooleanExpression contains(EnumPath path, String value, boolean ignoreCase) {
		if (EPFactory.isSupportsUnTypedValues()) {
			return path.in((List)EnumUtils.getEnumList(path.getType())
				.stream()
				.filter(
					v -> ignoreCase 
					? StringUtils.containsIgnoreCase(v.toString(), value)
					: StringUtils.contains(v.toString(), value))
				.collect(Collectors.toList()));
		} else {
			throw new UnsupportedOperationException(
				MessageFormat.format(
					"EPOperator: {0} not supported with Enum values",
					new Object[]{EPOperator.CONTAINS}));
		}
	}

	@Override protected BooleanExpression like(EnumPath path, String value, boolean ignoreCase) {
		throw new UnsupportedOperationException("Enum value can't be searched using like operator");
	}

	@Override protected BooleanExpression startsWith(EnumPath path, String value, boolean ignoreCase) {
		if (EPFactory.isSupportsUnTypedValues()) {
			return path.in((List)EnumUtils.getEnumList(path.getType())
				.stream()
				.filter(
					v -> ignoreCase 
					? StringUtils.startsWithIgnoreCase(v.toString(), value)
					: StringUtils.startsWith(v.toString(), value))
				.collect(Collectors.toList()));
		} else {
			throw new UnsupportedOperationException(
				MessageFormat.format(
					"EPOperator: {0} not supported with Enum values",
					new Object[]{EPOperator.STARTS_WITH}));
		}
	}

	@Override protected BooleanExpression endsWith(EnumPath path, String value, boolean ignoreCase) {
		if (EPFactory.isSupportsUnTypedValues()) {
			return path.in((List)EnumUtils.getEnumList(path.getType())
				.stream()
				.filter(
					v -> ignoreCase 
					? StringUtils.endsWithIgnoreCase(v.toString(), value)
					: StringUtils.endsWith(v.toString(), value))
				.collect(Collectors.toList()));
		} else {
			throw new UnsupportedOperationException(
				MessageFormat.format(
					"EPOperator: {0} not supported with Enum values",
					new Object[]{EPOperator.ENDS_WITH}));
		}
	}

	@Override protected BooleanExpression matches(EnumPath path, String value) {
		if (EPFactory.isSupportsUnTypedValues()) {
			return path.in((List)EnumUtils.getEnumList(path.getType())
				.stream()
				.filter(
					v -> regex_pattern_cache.getUnchecked(value).matcher(v.toString()).matches())
				.collect(Collectors.toList()));
		} else {
			throw new UnsupportedOperationException(
				MessageFormat.format(
					"EPOperator: {0} not supported with Enum values",
					new Object[]{EPOperator.MATCHES}));
		}
	}

	@Override protected BooleanExpression gt(EnumPath path, String value) {
		throw new UnsupportedOperationException("Enum value can't be searched using gt operator");
	}

	@Override protected BooleanExpression gte(EnumPath path, String value) {
		throw new UnsupportedOperationException("Enum value can't be searched using gte operator");
	}

	@Override protected BooleanExpression lt(EnumPath path, String value) {
		throw new UnsupportedOperationException("Enum value can't be searched using lt operator");
	}

	@Override protected BooleanExpression lte(EnumPath path, String value) {
		throw new UnsupportedOperationException("Enum value can't be searched using lte operator");
	}
}
