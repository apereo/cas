package org.apereo.cas.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link CollectionUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public final class CollectionUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionUtils.class);

    private CollectionUtils() {
    }

    /**
     * Convert the object given into a {@link Collection} instead.
     *
     * @param obj the object to convert into a collection
     * @return The collection instance containing the object provided
     */
    @SuppressWarnings("unchecked")
    public static Set<Object> toCollection(final Object obj) {
        final Set<Object> c = new HashSet<>();
        if (obj == null) {
            LOGGER.debug("Converting null obj to empty collection");
        } else if (obj instanceof Collection) {
            c.addAll((Collection<Object>) obj);
            LOGGER.trace("Converting multi-valued attribute [{}]", obj);
        } else if (obj instanceof Map) {
            throw new UnsupportedOperationException(Map.class.getCanonicalName() + " is not supported");
        } else if (obj.getClass().isArray()) {
            Collections.addAll(c, obj);
            LOGGER.trace("Converting array attribute [{}]", obj);
        } else {
            c.add(obj);
            LOGGER.trace("Converting attribute [{}]", obj);
        }
        return c;
    }

    /**
     * Wraps a possibly null map in an immutable wrapper.
     *
     * @param <K>    the key type
     * @param <V>    the value type
     * @param source Nullable map to wrap.
     * @return {@link Collections#unmodifiableMap(java.util.Map)} if given map is not null, otherwise
     * {@link java.util.Collections#emptyMap()}.
     */
    public static <K, V> Map<K, V> wrap(final Map<K, V> source) {
        if (source != null) {
            return new HashMap<>(source);
        }
        return new HashMap<>();
    }

    /**
     * Wraps a possibly null list in an immutable wrapper.
     *
     * @param <T>    the type parameter
     * @param source Nullable list to wrap.
     * @return {@link Collections#unmodifiableList(List)} if given list is not null, otherwise {@link java.util.Collections#EMPTY_LIST}.
     */
    public static <T> List<T> wrap(final List<T> source) {
        final List<T> list = new ArrayList<>();
        if (source != null) {
            list.addAll(source);
        }
        return list;
    }

    /**
     * Wrap set.
     *
     * @param <T>    the type parameter
     * @param values the values
     * @return the set
     */
    public static <T> Set<T> wrap(final Object... values) {
        final Set<T> list = new LinkedHashSet<>();
        if (values != null && values.length > 0) {
            final Set set = Arrays.stream(values).collect(Collectors.toSet());
            list.addAll(set);
        }
        return list;
    }

    /**
     * Wrap varargs.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @return the set
     */
    public static <T> Set<T> wrap(final Set<T> source) {
        final Set<T> list = new LinkedHashSet<>();
        if (source != null) {
            list.addAll(source);
        }
        return list;
    }
}
