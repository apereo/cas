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
@SuppressWarnings({"rawtypes", "unchecked"})
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
        kryo.writeClassAndObject(output, view.getCopy().apply(object));
    }

    @Override
    public Object read(final Kryo kryo, final Input input, final Class<?> type) {
        return view.getWrapper().apply(kryo.readClassAndObject(input));
    }

    @Override
    public Object copy(final Kryo kryo, final Object original) {
        return view.getWrapper().apply(kryo.copy(view.getCopy().apply(original)));
    }

    @Getter
    @RequiredArgsConstructor
    private enum View {
        COLLECTION(Collections.unmodifiableCollection(new ArrayList<>()).getClass(),
            value -> new ArrayList<>((Collection) value), value -> Collections.unmodifiableCollection((Collection) value)),
        RANDOM_ACCESS_LIST(Collections.unmodifiableList(new ArrayList<>()).getClass(),
            value -> new ArrayList<>((List) value), value -> Collections.unmodifiableList((List) value)),
        LIST(Collections.unmodifiableList(new LinkedList<>()).getClass(),
            value -> new LinkedList<>((List) value), value -> Collections.unmodifiableList((List) value)),
        SET(Collections.unmodifiableSet(new HashSet<>()).getClass(),
            value -> new LinkedHashSet<>((Set) value), value -> Collections.unmodifiableSet((Set) value)),
        SORTED_SET(Collections.unmodifiableSortedSet(new TreeSet<>()).getClass(),
            value -> new TreeSet<>((SortedSet) value), value -> Collections.unmodifiableSortedSet((SortedSet) value)),
        MAP(Collections.unmodifiableMap(new HashMap<>()).getClass(),
            value -> new LinkedHashMap<>((Map) value), value -> Collections.unmodifiableMap((Map) value)),
        SORTED_MAP(Collections.unmodifiableSortedMap(new TreeMap<>()).getClass(),
            value -> new TreeMap<>((SortedMap) value), value -> Collections.unmodifiableSortedMap((SortedMap) value));

        private final Class<?> type;

        private final Function<Object, Object> copy;

        private final Function<Object, Object> wrapper;
    }
}
