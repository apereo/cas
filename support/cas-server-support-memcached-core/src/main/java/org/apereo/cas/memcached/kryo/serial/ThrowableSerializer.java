package org.apereo.cas.memcached.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link ThrowableSerializer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ThrowableSerializer extends Serializer<Throwable> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThrowableSerializer.class);

    @Override
    public void write(final Kryo kryo, final Output output, final Throwable object) {
        kryo.writeObject(output, object.getClass());
        kryo.writeObject(output, StringUtils.defaultIfBlank(object.getMessage(), StringUtils.EMPTY));
    }

    @Override
    public Throwable read(final Kryo kryo, final Input input, final Class<Throwable> type) {
        try {
            final Class clazz = kryo.readObject(input, Class.class);
            final String msg = kryo.readObject(input, String.class);
            final Throwable throwable = (Throwable) clazz.getDeclaredConstructor(String.class).newInstance(msg);
            return throwable;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new Throwable();
    }
}
