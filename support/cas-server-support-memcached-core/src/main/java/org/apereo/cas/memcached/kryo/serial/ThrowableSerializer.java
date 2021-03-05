package org.apereo.cas.memcached.kryo.serial;

import org.apereo.cas.util.LoggingUtils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link ThrowableSerializer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class ThrowableSerializer extends Serializer<Throwable> {


    @Override
    public void write(final Kryo kryo, final Output output, final Throwable object) {
        kryo.writeObject(output, object.getClass());
        kryo.writeObject(output, StringUtils.defaultIfBlank(object.getMessage(), StringUtils.EMPTY));
    }

    @Override
    public Throwable read(final Kryo kryo, final Input input, final Class<? extends Throwable> aClass) {
        try {
            val clazz = kryo.readObject(input, Class.class);
            val msg = kryo.readObject(input, String.class);
            return (Throwable) clazz.getDeclaredConstructor(String.class).newInstance(msg);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new Throwable();
    }
}
