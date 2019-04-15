package org.apereo.cas.memcached.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link ImmutableNativeJavaMapSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class ImmutableNativeJavaMapSerializer extends Serializer<Map<Object, ? extends Object>> {

    public ImmutableNativeJavaMapSerializer() {
        super(true, true);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final Map<Object, ?> objectMap) {
        kryo.writeObject(output, new HashMap<>(objectMap));
    }

    @Override
    public Map<Object, ? extends Object> read(final Kryo kryo, final Input input, final Class<Map<Object, ?>> aClass) {
        val map = kryo.readObject(input, HashMap.class);
        return new HashMap<>(map);
    }
}
