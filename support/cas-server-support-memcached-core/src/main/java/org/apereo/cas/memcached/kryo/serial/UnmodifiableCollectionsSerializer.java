package org.apereo.cas.memcached.kryo.serial;

import module java.base;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Kryo serializer for the unmodifiable views of {@link Collections} that does not reflect on the private
 * field holding the wrapped collection, so it works without opening {@code java.util} to the application.
 * The contents of the view are written as a modifiable copy and wrapped again when read.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings({"rawtypes", "unchecked", "JdkObsolete"})
public class UnmodifiableCollectionsSerializer extends Serializer<Object> {

    private final View view;

    /**
     * Register this serializer with Kryo for every unmodifiable view type of {@link Collections}.
     *
     * @param kryo the kryo instance
     */
    public static void registerSerializers(final Kryo kryo) {
        for (val view : View.values()) {
            kryo.register(view.getType(), new UnmodifiableCollectionsSerializer(view));
        }
    }

    @Override
    public void write(final Kryo kryo, final Output output, final Object object) {
        kryo.writeClassAndObject(output, view.copy(object));
    }

    @Override
    public Object read(final Kryo kryo, final Input input, final Class<?> type) {
        return view.wrap(kryo.readClassAndObject(input));
    }

    @Override
    public Object copy(final Kryo kryo, final Object original) {
        return view.wrap(kryo.copy(view.copy(original)));
    }

    @Getter
    @RequiredArgsConstructor
    //CHECKSTYLE:OFF
    private enum View {
        COLLECTION(Collections.unmodifiableCollection(new ArrayList<>()).getClass()),
        RANDOM_ACCESS_LIST(Collections.unmodifiableList(new ArrayList<>()).getClass()),
        LIST(Collections.unmodifiableList(new LinkedList<>()).getClass()),
        SET(Collections.unmodifiableSet(new HashSet<>()).getClass()),
        SORTED_SET(Collections.unmodifiableSortedSet(new TreeSet<>()).getClass()),
        MAP(Collections.unmodifiableMap(new HashMap<>()).getClass()),
        SORTED_MAP(Collections.unmodifiableSortedMap(new TreeMap<>()).getClass());

        private final Class<?> type;

        /**
         * Copy the contents of an unmodifiable view into a modifiable collection or map of the same kind.
         *
         * @param value the unmodifiable view
         * @return the modifiable copy
         */
        Object copy(final Object value) {
            return switch (this) {
                case COLLECTION, RANDOM_ACCESS_LIST -> new ArrayList<>((Collection) value);
                case LIST -> new LinkedList<>((List) value);
                case SET -> new LinkedHashSet<>((Set) value);
                case SORTED_SET -> new TreeSet<>((SortedSet) value);
                case MAP -> new LinkedHashMap<>((Map) value);
                case SORTED_MAP -> new TreeMap<>((SortedMap) value);
            };
        }

        /**
         * Wrap a modifiable collection or map in the unmodifiable view of this kind.
         *
         * @param value the modifiable collection or map
         * @return the unmodifiable view
         */
        Object wrap(final Object value) {
            return switch (this) {
                case COLLECTION -> Collections.unmodifiableCollection((Collection) value);
                case RANDOM_ACCESS_LIST, LIST -> Collections.unmodifiableList((List) value);
                case SET -> Collections.unmodifiableSet((Set) value);
                case SORTED_SET -> Collections.unmodifiableSortedSet((SortedSet) value);
                case MAP -> Collections.unmodifiableMap((Map) value);
                case SORTED_MAP -> Collections.unmodifiableSortedMap((SortedMap) value);
            };
        }
    }
    //CHECKSTYLE:ON
}
