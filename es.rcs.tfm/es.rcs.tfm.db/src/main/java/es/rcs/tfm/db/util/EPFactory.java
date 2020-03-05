/*******************************************************************************
 * Copyright (c) 2018 @gt_tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package es.rcs.tfm.db.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.QuerydslBindings.PathBinder;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

/**
 * Main entry point for library consumers. Factory class provides access to
 * appropriate {@link EP} based on provided {@link Path} type.
 *
 * @author gt_tech
 */
@SuppressWarnings( {"unchecked", "rawtypes"} )
public final class EPFactory {

    /*
    Internal cache for EP objects
     */
	static LoadingCache<Path, EP> loadingCache = CacheBuilder.newBuilder()
        .build(new CacheLoader<Path, EP>() {
            @Override
            public EP load(Path key) throws Exception {
                if (StringPath.class.isAssignableFrom(key.getClass())) {
                    return new EPStringImpl();
                } else if (EnumPath.class.isAssignableFrom(key.getClass())) {
                    return new EPEnumImpl();
                } else if (NumberPath.class.isAssignableFrom(key.getClass())) {
                    //return new EPNumberImpl();
                	return new EPStringImpl();
                }
                return null;
            }
        });

    private static boolean supportsUnTypedValues = false;

    /*
     * Registry for storing path to alias mapping.
     */
    private static Map<Path, String> path_alias_registry = new HashMap<>();

    /**
     * Returns an {@link Optional} of {@link EP} if available for the provided {@code Path} instance.
     *
     * @param path <code>Path</code> for which <code>EP</code> is to be returned.
     * @return <code>Optional</code> containing <code>EP</code> if available or else an empty optional.
     */
    public static Optional<EP> getProvider(Path path) {
        return Optional.ofNullable(loadingCache.getUnchecked(path));
    }

    /**
     * Create a predicate based on implementation specific logic's processing of
     * supplied value(s).
     *
     * @param path  <code>Path</code> path
     * @param value Input value(s) (Must not be {@link Optional}, can be a primitive or collection
     * @return {@link Optional} of {@link Predicate} based on provided value.
     */
	public static Optional<Predicate> getPredicate(Path path, Object value) {
        return Optional.ofNullable(loadingCache.getUnchecked(path))
                .flatMap(p -> p.getPredicate(path, value));
    }

    /**
     * Method registers the new alias for given Path. It is assumed that
     * {@link PathBinder} available from {@link QuerydslBindings} is also
     * updated with new alias prior to registering here.
     *
     * @param path  {@link Path} on which alias is applied
     * @param alias String alias value for supplied path
     */
    public static void registerAlias(Path path, String alias) {
        if (path != null && StringUtils.isNotEmpty(alias)) {
            path_alias_registry.put(path, alias);
        }
    }

    /**
     * @param path Path for which alias to be looked up from local registry.
     * @return {@link Optional} of alias if available, otherwise empty
     * {@link Optional}
     */
    public static Optional<String> findAlias(Path path) {
        return Optional.ofNullable(path)
               .map(p -> path_alias_registry.get(p));
    }

    /**
     * @return <code>true</code> when experimental features are turned on, implying that untyped
     * values are going to be made available to {@link EP} for
     * non-string paths, <code>false</code> is returned if experimental features are disabled
     */
    public static boolean isSupportsUnTypedValues() {
        return supportsUnTypedValues;
    }

    /**
     * Sets whether experimental features are turned on, implying that untyped
     * values are going to be made available to {@link EP} for
     * non-string paths.
     *
     * @param supportsUnTypedValues <code>Boolean</code> indicating status of support of untyped values (aka. experimental features)
     */
    public static void setSupportsUnTypedValues(boolean supportsUnTypedValues) {
        EPFactory.supportsUnTypedValues = supportsUnTypedValues;
    }

}