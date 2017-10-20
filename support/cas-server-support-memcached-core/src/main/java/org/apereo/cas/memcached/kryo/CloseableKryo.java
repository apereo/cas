package org.apereo.cas.memcached.kryo;

import com.esotericsoftware.kryo.Kryo;

import java.io.Closeable;

/**
 * This is {@link CloseableKryo} which allows {@link Kryo} instances
 * to be used with try-resource blocks.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CloseableKryo extends Kryo implements Closeable {
    private final CasKryoPool kryoPool;

    public CloseableKryo(final CasKryoPool autoKryoPool) {
        this.kryoPool = autoKryoPool;
    }

    @Override
    public void close() {
        this.kryoPool.release(this);
    }
}
