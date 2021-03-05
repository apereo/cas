package org.apereo.cas.memcached.kryo;

/**
 * This is {@link KryoPool}.
 *
 * @author Misagh Moayyed
 * @param <T> the type parameter
 * @since 6.3.0
 */
public interface KryoPool<T extends CloseableKryo> {
    /**
     * Borrow.
     *
     * @return the t
     */
    T borrow();

    /**
     * Release.
     *
     * @param kryo the kryo
     */
    void release(T kryo);
}
