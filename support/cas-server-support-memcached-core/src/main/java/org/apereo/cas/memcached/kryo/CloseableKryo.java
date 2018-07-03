package org.apereo.cas.memcached.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.Serializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;

/**
 * This is {@link CloseableKryo} which allows {@link Kryo} instances
 * to be used with try-resource blocks.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class CloseableKryo extends Kryo implements Closeable {
    private final CasKryoPool kryoPool;

    @Override
    public void close() {
        this.kryoPool.release(this);
    }

    @Override
    public Registration register(final Class type, final Serializer serializer) {
        LOGGER.debug("Registering class [{}] with Kryo using serializer [{}]", type.getName(), serializer.getClass().getName());
        return super.register(type, serializer);
    }

    @Override
    public Registration register(final Registration registration) {
        LOGGER.debug("Registering class [{}] with Kryo using id [{}]", registration.getType().getName(), registration.getId());
        return super.register(registration);
    }
}
