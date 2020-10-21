package org.apereo.cas.memcached.kryo;

/**
 * This is {@link KryoPool}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface KryoPool<T extends CloseableKryo> {
    T borrow();

    void release(T kryo);
}
