package es.rcs.tfm.db.util;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;

public class EPQueryParse {

	public static final Pattern OPERATORS_PTRN = Pattern.compile("(\\w+)\\s*[\\(\\[](.+)[\\)\\]]\\s*");
	public static final Pattern NUMBERS_RANGE_PTRN = Pattern.compile("\\s*(\\d+)\\s*?\\-\\s*(\\d+)\\s*");
	public static final Pattern STRINGS_RANGE_PTRN = Pattern.compile("(.*)\\-(.*)");
	public static final String OPERATORS_EQ = "equals";
	public static final String OPERATORS_NE = "not equals";
	public static final String OPERATORS_GT = "greaterThan";
	public static final String OPERATORS_GE = "greaterThanOrEqual";
	public static final String OPERATORS_LT = "lessThan";
	public static final String OPERATORS_LE = "lessThanOrEqual";
	public static final String OPERATORS_SW = "startsWith";
	public static final String OPERATORS_EW = "endsWith";
	public static final String OPERATORS_CO = "contains";
	public static final String OPERATORS_NC = "notContains";
	public static final String OPERATORS_IR = "InRange";

	public static <T extends Comparable<?>> Optional<Predicate> getBinding(Path<T> path, Collection<? extends T> values) {
		// StringPath
		// EnumPath
		// NumberPath
		BooleanBuilder predicatesAndBuilder = new BooleanBuilder();
		for (T value: values) {
			
			BooleanBuilder predicatesOr = new BooleanBuilder();
			Boolean predicatesOrHay = false;
			Boolean predicatesNot = false;
			
			Matcher match = OPERATORS_PTRN.matcher(value.toString());
			if (match.find()) {
				String op = match.group(1);
				String expr = match.group(2);
				
				if (OPERATORS_EQ.equals(op) || OPERATORS_NE.equals(op)) {

					predicatesNot = OPERATORS_NE.equals(op);

					String[] strs = expr.split("\\s*\\,\\s*");
					for (String s: strs) {
						Matcher m = NUMBERS_RANGE_PTRN.matcher(s);
						//Integer izq = null;
						//Integer der = null;
						if (m.find()) {
							/*
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
							*/
						}
					}
				} else if (OPERATORS_GT.equals(op)) {
					//expr = path.gt(str);
				} else if (OPERATORS_GE.equals(op)) {
					//expr = path.goe(str);
				} else if (OPERATORS_LT.equals(op)) {
					//expr = path.lt(str);
				} else if (OPERATORS_LE.equals(op)) {
					//expr = path.loe(str);
				} else if (OPERATORS_SW.equals(op)) {
					
					String[] strs = expr.split("\\s*\\,\\s*");
					for (String str: strs) {
						
						if (StringUtils.isNotEmpty(str)) {
				            BooleanBuilder predicate = new BooleanBuilder();
				            StringPath strPath = (StringPath)path;
				            predicate.or(strPath.startsWithIgnoreCase(str));
	
				            predicatesOrHay = true;
							predicatesOr.or(predicate);
						}

					}

				} else if (OPERATORS_EW.equals(op)) {
					
					String[] strs = expr.split("\\s*\\,\\s*");
					for (String str: strs) {
						
						if (StringUtils.isNotEmpty(str)) {
				            BooleanBuilder predicate = new BooleanBuilder();
				            StringPath strPath = (StringPath)path;
				            predicate.or(strPath.endsWithIgnoreCase(str));
	
				            predicatesOrHay = true;
							predicatesOr.or(predicate);
						}

					}
				} else if (OPERATORS_CO.equals(op) || OPERATORS_NC.equals(op)) {

					predicatesNot = OPERATORS_NC.equals(op);
					
					String withoutComillas = expr.replaceAll("\"", "");
					String[] strs = withoutComillas.split("\\s*\\,\\s*");
					for (String str: strs) {
						Matcher m = STRINGS_RANGE_PTRN.matcher(str);
						if (m.find()) {
							
							String from = new String(m.group(1));
							String to = new String(m.group(2));
							
				            BooleanBuilder predicate = new BooleanBuilder();
				            StringPath strPath = (StringPath)path;
				            predicate.or(strPath.between(from, to));

				            predicatesOrHay = true;
							predicatesOr.or(predicate);

						} else {

							// replace espacios con % 
							str = "%" + str.trim().replaceAll(" ", "%") + "%";
							
				            BooleanBuilder predicate = new BooleanBuilder();
				            StringPath strPath = (StringPath)path;
				            predicate.or(strPath.likeIgnoreCase(str));

				            predicatesOrHay = true;
							predicatesOr.or(predicate);
							
						}
						
					}
					//expr = path.likeIgnoreCase(str);
				} else if (OPERATORS_IR.equals(op)) {
					//expr = path.containsIgnoreCase(str);
				}
			
			}
			if (predicatesOrHay) {
				if (predicatesNot) {
					predicatesAndBuilder.andNot(predicatesOr);
				} else {
					predicatesAndBuilder.and(predicatesOr);
				}
			}
			
		}

		return Optional.of(predicatesAndBuilder);
	}

	/*
	 * 

	bindings
		.bind(root.codigo).all((path, value) -> {
	        Iterator<? extends Long> it = value.iterator();
	        path.contains(right);
	        path.containsIgnoreCase(right);
	        path.startsWith(right);
	        path.startsWithIgnoreCase(right);
	        path.endsWith(right);
	        path.endsWithIgnoreCase(right);
	        path.like(right);
	        path.likeWithIgnoreCase(right);
	        path.notLike(str);
	        path.eq(right);
	        path.eqAll(right);
	        path.eqAny(right);
	        path.ne(right);
	        path.neAll(right);
	        path.neAny(right);
	        path.notEqualsIgnoreCase(right);
	        path.goe(right);
	        path.goeAll(right);
	        path.goeAny(right);
	        path.gt(right);
	        path.gtAll(right);
	        path.gtAny(right);
	        path.loe(right);
	        path.loeAll(right);
	        path.loeAny(right);
	        path.lt(right);
	        path.ltAll(right);
	        path.ltAny(right);
	        path.in(right);
	        path.isNotNull();
	        path.isNull();
	        path.in(right);
	        path.notIn(right);
	        path.between(from, to)
	        path.notBetween(from, to)
	        return path.between(it.next(), it.next());
    });
    bindings.bind(String.class).all((StringPath path, Collection<? extends String> values) -> {
	    BooleanBuilder predicate = new BooleanBuilder();
        // oneliner with Java 8 forEach
        values.forEach(value->predicate.or(path.containsIgnoreCase(value) );
    });
			.all((DateTimePath<Date> path, Collection<? extends Date> values) -> {
				
			});
	bindings
		.bind(Boolean.class)
			.all((BooleanPath path, Collection<? extends Boolean> value) -> {
				return path;
			});
	bindings
		.bind(Boolean.class)
			.all((BooleanPath path, Collection<? extends Boolean> value) -> {
				return path;
			});
	bindings
		.bind(Date.class)
			.all((DatePath<Date> path, Collection<? extends Date> values) -> {
				return path;
			});
	bindings
		.bind(Date.class)
			.all((DateTimePath<Date> path, Collection<? extends Date> values) -> {
				return path;
			});
    */
	
}