package org.apereo.cas.redis.core;

import module java.base;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;

/**
 * This is {@link CasRedisTemplate}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public interface CasRedisTemplate<K, V> extends RedisOperations<K, V> {
    /**
     * Keys stream.
     *
     * @param pattern the pattern
     * @param count   the count
     * @return the stream
     */
    Stream<K> scan(String pattern, Long count);

    /**
     * Scan stream.
     *
     * @param pattern the pattern
     * @return the stream
     */
    default Stream<K> scan(final String pattern) {
        return scan(pattern, -1L);
    }

    /**
     * Every key that matches the pattern, read from Redis in large batches and streamed as they arrive, for counting
     * and grouping over many keys. Keys are not kept to drop duplicates, so a key may, rarely, come twice while Redis
     * resizes its keyspace.
     *
     * @param pattern the pattern
     * @return the stream, to be closed once read
     */
    Stream<K> scanAll(String pattern);

    /**
     * Count entries.
     *
     * @param pattern the pattern
     * @return the long
     */
    long count(String pattern);

    /**
     * Initialize.
     */
    void initialize();

    /**
     * Gets connection factory.
     *
     * @return the connection factory
     */
    RedisConnectionFactory getConnectionFactory();
}
