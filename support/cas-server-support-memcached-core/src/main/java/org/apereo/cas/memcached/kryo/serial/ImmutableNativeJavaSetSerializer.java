package org.apereo.cas.memcached.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.val;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link ImmutableNativeJavaSetSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class ImmutableNativeJavaSetSerializer extends Serializer<Set<Object>> {
    public ImmutableNativeJavaSetSerializer() {
        super(false, true);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final Set<Object> object) {
        output.writeInt(object.size(), true);
        for (val elm : object) {
            kryo.writeClassAndObject(output, elm);
        }
    }

    @Override
    public Set<Object> read(final Kryo kryo, final Input input, final Class<Set<Object>> aClass) {
        val size = input.readInt(true);
        val list = new Object[size];
        for (var i = 0; i < size; ++i) {
            list[i] = kryo.readClassAndObject(input);
        }
        return Arrays.stream(list).collect(Collectors.toSet());
    }
}
