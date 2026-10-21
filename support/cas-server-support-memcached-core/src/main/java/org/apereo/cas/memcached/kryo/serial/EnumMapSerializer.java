package org.apereo.cas.memcached.kryo.serial;

import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.val;

/**
 * Kryo serializer for {@link EnumMap} that does not reflect on the private key type of the map,
 * so it works without opening {@code java.util} to the application. A map with entries is written
 * as the key enum type followed by the ordinal and value of each entry. An empty map carries its key type
 * only in its Java serialized form, so it is written that way and read back through a filter that accepts
 * nothing but the map and its key enum type.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class EnumMapSerializer extends Serializer<EnumMap> {

    private static final ObjectInputFilter EMPTY_MAP_FILTER = info -> {
        if (info.depth() > 3 || info.references() > 16) {
            return ObjectInputFilter.Status.REJECTED;
        }
        val serialClass = info.serialClass();
        if (serialClass == null || serialClass == EnumMap.class || serialClass.isEnum() || serialClass == Enum.class) {
            return ObjectInputFilter.Status.ALLOWED;
        }
        return ObjectInputFilter.Status.REJECTED;
    };

    @Override
    public void write(final Kryo kryo, final Output output, final EnumMap map) {
        output.writeBoolean(map.isEmpty());
        if (map.isEmpty()) {
            val bytes = FunctionUtils.doUnchecked(() -> {
                try (val buffer = new ByteArrayOutputStream(); val stream = new ObjectOutputStream(buffer)) {
                    stream.writeObject(map);
                    stream.flush();
                    return buffer.toByteArray();
                }
            });
            output.writeVarInt(bytes.length, true);
            output.writeBytes(bytes);
            return;
        }
        val entries = (Set<Map.Entry<Enum, Object>>) map.entrySet();
        kryo.writeClass(output, entries.iterator().next().getKey().getDeclaringClass());
        output.writeVarInt(map.size(), true);
        for (val entry : entries) {
            output.writeVarInt(entry.getKey().ordinal(), true);
            kryo.writeClassAndObject(output, entry.getValue());
        }
    }

    @Override
    public EnumMap read(final Kryo kryo, final Input input, final Class<? extends EnumMap> type) {
        if (input.readBoolean()) {
            val bytes = input.readBytes(input.readVarInt(true));
            return FunctionUtils.doUnchecked(() -> {
                try (val stream = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                    stream.setObjectInputFilter(EMPTY_MAP_FILTER);
                    return (EnumMap) stream.readObject();
                }
            });
        }
        val keyType = (Class<Enum>) kryo.readClass(input).getType();
        val constants = keyType.getEnumConstants();
        val result = new EnumMap(keyType);
        kryo.reference(result);
        val size = input.readVarInt(true);
        for (var i = 0; i < size; i++) {
            val key = constants[input.readVarInt(true)];
            result.put(key, kryo.readClassAndObject(input));
        }
        return result;
    }

    @Override
    public EnumMap copy(final Kryo kryo, final EnumMap original) {
        val copy = new EnumMap(original);
        copy.replaceAll((key, value) -> kryo.copy(value));
        return copy;
    }
}
