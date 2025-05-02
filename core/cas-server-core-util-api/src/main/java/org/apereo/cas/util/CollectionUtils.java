package org.apereo.cas.util;

import org.apereo.cas.util.function.FunctionUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Multimap;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
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
     * Ensure given attributes in the map are always captured
     * with a single-value, if they exist. If the requested attributes have multiple values,
     * the first element is chosen.
     *
     * @param attributes             the attributes
     * @param singleValuedAttributes the single valued attributes
     * @return the map
     */
    public static Map<String, Object> toSingleValuedMap(final Map<String, Object> attributes,
                                                        final List<String> singleValuedAttributes) {
        val results = new LinkedHashMap<>(attributes);
        singleValuedAttributes.forEach(name -> {
            val values = results.remove(name);
            firstElement(values).ifPresent(value -> results.put(name, value));
        });
        return results;
    }

    /**
     * Convert attribute values to multi valued objects.
     *
     * @param attributes the attributes
     * @return the map of attributes to return
     */
    public static Map<String, List<Object>> toMultiValuedMap(final Map<String, Object> attributes) {
        val entries = attributes.entrySet();
        return entries
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                val value = entry.getValue();
                return toCollection(value, ArrayList.class);
            }));
    }

    /**
     * From comma delimited values.
     *
     * @param attributes the attributes
     * @return the map
     */
    public static Map<String, List<Object>> fromCommaDelimitedValues(final Map<String, String> attributes) {
        val entries = attributes.entrySet();
        return entries
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                val value = org.springframework.util.StringUtils.commaDelimitedListToSet(entry.getValue());
                return toCollection(value, ArrayList.class);
            }));
    }

    /**
     * Distinct by key predicate.
     *
     * @param <T>          the type parameter
     * @param keyExtractor the key extractor
     * @return the predicate
     */
    public static <T> Predicate<T> distinctByKey(final Function<? super T, ?> keyExtractor) {
        val seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * Converts the provided object into a collection
     * and return the first element, or empty.
     *
     * @param obj the obj
     * @return the optional
     */
    public static Optional<Object> firstElement(final Object obj) {
        val object = toCollection(obj);
        if (object.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(object.iterator().next());
    }

    /**
     * Converts the provided object into a collection
     * and return the first element, or empty.
     *
     * @param <T>   the type parameter
     * @param obj   the obj
     * @param clazz the clazz
     * @return the optional
     */
    public static <T> Optional<T> firstElement(final Object obj, final Class<T> clazz) {
        var result = (Optional<T>) firstElement(obj);
        result.ifPresent(value -> Assert.isTrue(clazz.isAssignableFrom(value.getClass()), () -> "Invalid element subtype"));
        return result;
    }

    /**
     * To collection t.
     *
     * @param <T>   the type parameter
     * @param obj   the obj
     * @param clazz the clazz
     * @return the t
     */
    public static <T extends Collection> T toCollection(final Object obj, final Class<T> clazz) {
        return FunctionUtils.doUnchecked(() -> {
            val results = toCollection(obj);
            if (clazz.isInterface()) {
                throw new IllegalArgumentException("Cannot accept an interface " + clazz.getSimpleName() + " to create a new object instance");
            }
            val col = clazz.getDeclaredConstructor().newInstance();
            col.addAll(results);
            return col;
        });
    }

    /**
     * Convert the object given into a {@link Collection} instead.
     *
     * @param obj the object to convert into a collection
     * @return The collection instance containing the object provided
     */
    @SuppressWarnings("JdkObsolete")
    public static Set<Object> toCollection(final Object obj) {
        val resultingSet = new LinkedHashSet<>();
        if (obj == null) {
            LOGGER.trace("Converting null obj to empty collection");
        } else if (obj instanceof final Collection values) {
            resultingSet.addAll(values);
            LOGGER.trace("Converting multi-valued element [{}]", obj);
        } else if (obj instanceof final Map map) {
            val set = (Set<Map.Entry>) map.entrySet();
            resultingSet.addAll(set.stream().map(e -> Pair.of(e.getKey(), e.getValue())).collect(Collectors.toSet()));
        } else if (obj.getClass().isArray()) {
            if (obj instanceof byte[]) {
                resultingSet.add(obj);
            } else {
                resultingSet.addAll(Arrays.stream((Object[]) obj).collect(Collectors.toSet()));
            }
            LOGGER.trace("Converting array element [{}]", obj);
        } else if (obj instanceof final Iterator it) {
            while (it.hasNext()) {
                resultingSet.add(it.next());
            }
        } else if (obj instanceof final Enumeration it) {
            while (it.hasMoreElements()) {
                resultingSet.add(it.nextElement());
            }
        } else {
            resultingSet.add(obj);
            LOGGER.trace("Converting element [{}]", obj);
        }
        return resultingSet.stream().filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
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
            val inner = source.asMap();
            val map = new HashMap<>();
            inner.forEach((k, v) -> map.put(k, wrap(v)));
            return (Map) map;
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
        val map = new LinkedHashMap();
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
    public static <K extends String, V> Map<K, V> wrap(final String key, final Object value,
                                                       final String key2, final Object value2) {
        val m = wrap(key, value);
        if (value2 != null && StringUtils.isNotBlank(key2)) {
            m.put(key2, value2);
        }
        return (Map) m;
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
        val m = wrap(key, value, key2, value2);
        if (value3 != null && StringUtils.isNotBlank(key3)) {
            m.put(key3, value3);
        }
        return (Map) m;
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
        val m = wrap(key, value, key2, value2, key3, value3);
        if (value4 != null && StringUtils.isNotBlank(key4)) {
            m.put(key4, value4);
        }
        return (Map) m;
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
        val m = wrap(key, value, key2, value2, key3, value3, key4, value4);
        if (value5 != null && StringUtils.isNotBlank(key5)) {
            m.put(key5, value5);
        }
        return (Map) m;
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
     * @param key6   the key 6
     * @param value6 the value 6
     * @return the map
     */
    public static <K, V> Map<K, V> wrap(final String key, final Object value,
                                        final String key2, final Object value2,
                                        final String key3, final Object value3,
                                        final String key4, final Object value4,
                                        final String key5, final Object value5,
                                        final String key6, final Object value6) {
        val m = wrap(key, value, key2, value2, key3, value3, key4, value4, key5, value5);
        if (value6 != null && StringUtils.isNotBlank(key6)) {
            m.put(key6, value6);
        }
        return (Map) m;
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
     * @param key6   the key 6
     * @param value6 the value 6
     * @param key7   the key 7
     * @param value7 the value 7
     * @return the map
     */
    public static <K, V> Map<K, V> wrap(final String key, final Object value,
                                        final String key2, final Object value2,
                                        final String key3, final Object value3,
                                        final String key4, final Object value4,
                                        final String key5, final Object value5,
                                        final String key6, final Object value6,
                                        final String key7, final Object value7) {
        val m = wrap(key, value, key2, value2, key3, value3, key4, value4, key5, value5, key6, value6);
        if (value7 != null && StringUtils.isNotBlank(key7)) {
            m.put(key7, value7);
        }
        return (Map) m;
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
     * @param key6   the key 6
     * @param value6 the value 6
     * @param key7   the key 7
     * @param value7 the value 7
     * @param key8   the key 8
     * @param value8 the value 8
     * @return the map
     */
    public static <K, V> Map<K, V> wrap(final String key, final Object value,
                                        final String key2, final Object value2,
                                        final String key3, final Object value3,
                                        final String key4, final Object value4,
                                        final String key5, final Object value5,
                                        final String key6, final Object value6,
                                        final String key7, final Object value7,
                                        final String key8, final Object value8) {
        val map = wrap(key, value, key2, value2, key3, value3, key4, value4,
            key5, value5, key6, value6, key7, value7);
        if (value8 != null && StringUtils.isNotBlank(key8)) {
            map.put(key8, value8);
        }
        return (Map) map;
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
     * @param key6   the key 6
     * @param value6 the value 6
     * @param key7   the key 7
     * @param value7 the value 7
     * @param key8   the key 8
     * @param value8 the value 8
     * @param key9   the key 9
     * @param value9 the value 9
     * @return the map
     */
    public static <K, V> Map<K, V> wrap(final String key, final Object value,
                                        final String key2, final Object value2,
                                        final String key3, final Object value3,
                                        final String key4, final Object value4,
                                        final String key5, final Object value5,
                                        final String key6, final Object value6,
                                        final String key7, final Object value7,
                                        final String key8, final Object value8,
                                        final String key9, final Object value9) {
        val map = wrap(key, value, key2, value2, key3, value3, key4, value4,
            key5, value5, key6, value6, key7, value7, key8, value8);
        if (value9 != null && StringUtils.isNotBlank(key9)) {
            map.put(key9, value9);
        }
        return (Map) map;
    }

    /**
     * Wraps a possibly null list in an immutable wrapper.
     *
     * @param <T>    the type parameter
     * @param source list to wrap.
     * @return the list
     */
    public static <T> List<T> wrap(final T source) {
        val list = new ArrayList<T>();
        if (source != null) {
            if (source instanceof final Collection values) {
                for (val value : values) {
                    list.add((T) value);
                }
            } else if (source.getClass().isArray()) {
                if (source.getClass().isAssignableFrom(byte[].class)) {
                    list.add(source);
                } else {
                    val elements = Arrays.stream((Object[]) source).toList();
                    list.addAll((Collection<? extends T>) elements);
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
     * @param source list to wrap.
     * @return the list
     */
    public static <T> List<T> wrap(final List<T> source) {
        val list = new ArrayList<T>();
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
        val list = new LinkedHashSet<T>();
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
        val list = new LinkedHashSet<T>();
        if (source != null) {
            list.add(source);
        }
        return list;
    }

    /**
     * Wrap set.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @return the set
     */
    @SafeVarargs
    public static <T> Set<T> wrapSet(final T... source) {
        val list = new LinkedHashSet<T>();
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
    @SafeVarargs
    public static <T> Set<T> wrapHashSet(final T... source) {
        val list = new HashSet<T>();
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
    public static <T> Set<T> wrapHashSet(final Collection<T> source) {
        return new HashSet<>(source);
    }

    /**
     * Wrap list.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @return the set
     */
    @SafeVarargs
    public static <T> List<T> wrapList(final T... source) {
        val list = new ArrayList<T>();
        addToCollection(list, source);
        return list;
    }

    /**
     * Wrap array list array list.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @return the array list
     */
    @SafeVarargs
    public static <T> List<T> wrapArrayList(final T... source) {
        val list = new ArrayList<T>();
        addToCollection(list, source);
        return list;
    }

    /**
     * Wrap linked hash map.
     *
     * @param <T>    the type parameter
     * @param key    the key
     * @param source the source
     * @return the array list
     */
    public static <T> Map<String, T> wrapLinkedHashMap(final String key, final T source) {
        val list = new LinkedHashMap<String, T>();
        list.put(key, source);
        return list;
    }

    /**
     * As multi value map.
     *
     * @param innerMap the inner map
     * @return the multi value map
     */
    public static MultiValueMap asMultiValueMap(final Map innerMap) {
        return org.springframework.util.CollectionUtils.toMultiValueMap(innerMap);
    }

    /**
     * As multi value map.
     *
     * @param key   the key
     * @param value the value
     * @return the multi value map
     */
    public static MultiValueMap asMultiValueMap(final String key, final Object value) {
        return org.springframework.util.CollectionUtils.toMultiValueMap(wrap(key, value));
    }

    /**
     * As multi value map.
     *
     * @param key1   the key 1
     * @param value1 the value 1
     * @param key2   the key 2
     * @param value2 the value 2
     * @return the multi value map
     */
    public static MultiValueMap asMultiValueMap(final String key1, final Object value1, final String key2, final Object value2) {
        val wrap = (Map) wrap(key1, wrapList(value1), key2, wrapList(value2));
        return org.springframework.util.CollectionUtils.toMultiValueMap(wrap);
    }

    /**
     * Convert directed list to map.
     *
     * @param inputList the input list
     * @return the map
     */
    public static Map<String, String> convertDirectedListToMap(final Collection<String> inputList) {
        val mappings = new TreeMap<String, String>();
        inputList
            .stream()
            .map(s -> {
                val bits = Splitter.on("->").splitToList(s);
                return Pair.of(bits.getFirst(), bits.size() > 1 ? bits.get(1) : StringUtils.EMPTY);
            })
            .forEach(p -> mappings.put(p.getKey(), p.getValue()));
        return mappings;
    }

    /**
     * Wrap collection.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @return the collection
     */
    @SafeVarargs
    public static <T> Collection<T> wrapCollection(final T... source) {
        val list = new LinkedHashSet<T>();
        addToCollection(list, source);
        return list;
    }

    private static <T> void addToCollection(final Collection<T> list, final T[] source) {
        if (source != null) {
            Arrays.stream(source).forEach(s -> {
                val col = toCollection(s);
                list.addAll((Collection) col);
            });
        }
    }

    /**
     * Merge map.
     *
     * @param attributes the attributes
     * @return the map
     */
    @SafeVarargs
    public static Map<String, Object> merge(final Map<String, ?>... attributes) {
        val result = new LinkedHashMap<String, Object>();
        Arrays.stream(attributes).forEach(result::putAll);
        return result;
    }
}
