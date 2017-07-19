package org.apereo.cas.util;

import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * Wrap map.
     *
     * @param <K>    the type parameter
     * @param <V>    the type parameter
     * @param source the source
     * @return the map
     */
    public static <K, V> Map<K, Collection<V>> wrap(final Multimap<K, V> source) {
        if (source != null && !source.isEmpty()) {
            final Map inner = source.asMap();
            final Map map = new HashMap<>();
            inner.forEach((k, v) -> map.put(k, wrap(v)));
            return map;
        }
        return new HashMap<>();
    }

    /**
     * Wraps a possibly null map in an immutable wrapper.
     *
     * @param <K>    the key type
     * @param <V>    the value type
     * @param source map to wrap.
     * @return the map
     */
    public static <K, V> Map<K, V> wrap(final Map<K, V> source) {
        if (source != null && !source.isEmpty()) {
            return new HashMap<>(source);
        }
        return new HashMap<>();
    }

    /**
     * Wrap map.
     *
     * @param <K>   the type parameter
     * @param <V>   the type parameter
     * @param key   the key
     * @param value the value
     * @return the map
     */
    public static <K, V> Map<K, V> wrap(final String key, final Object value) {
        final Map map = new HashMap<>();
        if (StringUtils.isNotBlank(key)) {
            map.put(key, value);
        }
        return map;
    }

    /**
     * Wrap map.
     *
     * @param <K>    the type parameter
     * @param <V>    the type parameter
     * @param key    the key
     * @param value  the value
     * @param key2   the key 2
     * @param value2 the value 2
     * @return the map
     */
    public static <K, V> Map<K, V> wrap(final String key, final Object value,
                                        final String key2, final Object value2) {
        final Map m = wrap(key, value);
        m.put(key2, value2);
        return m;
    }

    /**
     * Wrap map.
     *
     * @param <K>    the type parameter
     * @param <V>    the type parameter
     * @param key    the key
     * @param value  the value
     * @param key2   the key 2
     * @param value2 the value 2
     * @param key3   the key 3
     * @param value3 the value 3
     * @return the map
     */
    public static <K, V> Map<K, V> wrap(final String key, final Object value,
                                        final String key2, final Object value2,
                                        final String key3, final Object value3) {
        final Map m = wrap(key, value, key2, value2);
        m.put(key3, value3);
        return m;
    }

    /**
     * Wrap map.
     *
     * @param <K>    the type parameter
     * @param <V>    the type parameter
     * @param key    the key
     * @param value  the value
     * @param key2   the key 2
     * @param value2 the value 2
     * @param key3   the key 3
     * @param value3 the value 3
     * @param key4   the key 4
     * @param value4 the value 4
     * @return the map
     */
    public static <K, V> Map<K, V> wrap(final String key, final Object value,
                                        final String key2, final Object value2,
                                        final String key3, final Object value3,
                                        final String key4, final Object value4) {
        final Map m = wrap(key, value, key2, value2, key3, value3);
        m.put(key4, value4);
        return m;
    }

    /**
     * Wraps a possibly null list in an immutable wrapper.
     *
     * @param <T>    the type parameter
     * @param source Nullable list to wrap.
     * @return the list
     */
    public static <T> List<T> wrap(final T source) {
        final List<T> list = new ArrayList<>();
        if (source != null) {
            if (source instanceof Collection) {
                final Iterator it = ((Collection) source).iterator();
                while (it.hasNext()) {
                    list.add((T) it.next());
                }
            } else {
                list.add(source);
            }
        }
        return list;
    }

    /**
     * Wraps a possibly null list in an immutable wrapper.
     *
     * @param <T>    the type parameter
     * @param source Nullable list to wrap.
     * @return the list
     */
    public static <T> List<T> wrap(final List<T> source) {
        final List<T> list = new ArrayList<>();
        if (source != null && !source.isEmpty()) {
            list.addAll(source);
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
        if (source != null && !source.isEmpty()) {
            list.addAll(source);
        }
        return list;
    }

    /**
     * Wrap set set.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @return the set
     */
    public static <T> Set<T> wrapSet(final T source) {
        final Set<T> list = new LinkedHashSet<>();
        if (source != null) {
            list.add(source);
        }
        return list;
    }
}
