package org.apereo.cas.util;

import com.google.common.collect.Multimap;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link CollectionUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@UtilityClass
public class CollectionUtils {

    /**
     * Converts the provided object into a collection
     * and return the first element, or empty.
     *
     * @param obj the obj
     * @return the optional
     */
    public static Optional<Object> firstElement(final Object obj) {
        final Set<Object> object = CollectionUtils.toCollection(obj);
        if (object.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(object.iterator().next());
    }

    /**
     * Convert the object given into a {@link Collection} instead.
     *
     * @param obj the object to convert into a collection
     * @return The collection instance containing the object provided
     */
    public static Set<Object> toCollection(final Object obj) {
        final Set<Object> c = new LinkedHashSet<>();
        if (obj == null) {
            LOGGER.debug("Converting null obj to empty collection");
        } else if (obj instanceof Collection) {
            c.addAll((Collection<Object>) obj);
            LOGGER.trace("Converting multi-valued attribute [{}]", obj);
        } else if (obj instanceof Map) {
            final Set<Map.Entry> set = ((Map) obj).entrySet();
            c.addAll(set.stream().map(e -> Pair.of(e.getKey(), e.getValue())).collect(Collectors.toSet()));
        } else if (obj.getClass().isArray()) {
            c.addAll(Arrays.stream((Object[]) obj).collect(Collectors.toSet()));
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
    public static <K, V> Map<K, V> wrap(final Multimap<K, V> source) {
        if (source != null && !source.isEmpty()) {
            final Map inner = source.asMap();
            final Map map = new HashMap<>();
            inner.forEach((k, v) -> map.put(k, wrap(v)));
            return map;
        }
        return new HashMap<>(0);
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
        return new HashMap<>(0);
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
        final Map map = new LinkedHashMap();
        if (value != null && StringUtils.isNotBlank(key)) {
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
    public static <K extends String, V extends Object> Map<K, V> wrap(final String key, final Object value,
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
     * @param key5   the key 5
     * @param value5 the value 5
     * @return the map
     */
    public static <K, V> Map<K, V> wrap(final String key, final Object value,
                                        final String key2, final Object value2,
                                        final String key3, final Object value3,
                                        final String key4, final Object value4,
                                        final String key5, final Object value5) {
        final Map m = wrap(key, value, key2, value2, key3, value3, key4, value4);
        m.put(key5, value5);
        return m;
    }

    /**
     * Wraps a possibly null list in an immutable wrapper.
     *
     * @param <T>    the type parameter
     * @param source list to wrap.
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
            } else if (source.getClass().isArray()) {
                final List elements = Arrays.stream((Object[]) source).collect(Collectors.toList());
                list.addAll(elements);
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

    /**
     * Wrap set set.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @return the set
     */
    public static <T> Set<T> wrapSet(final T... source) {
        final Set<T> list = new LinkedHashSet<>();
        addToCollection(list, source);
        return list;
    }

    /**
     * Wrap hash set.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @return the set
     */
    public static <T> HashSet<T> wrapHashSet(final T... source) {
        final HashSet<T> list = new HashSet<>();
        addToCollection(list, source);
        return list;
    }

    /**
     * Wrap set set.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @return the set
     */
    public static <T> List<T> wrapList(final T... source) {
        final List<T> list = new ArrayList<>();
        addToCollection(list, source);
        return list;
    }

    private static <T> void addToCollection(final Collection<T> list, final T[] source) {
        if (source != null) {
            Arrays.stream(source).forEach(s -> {
                final Collection col = toCollection(s);
                list.addAll(col);
            });
        }
    }
}
