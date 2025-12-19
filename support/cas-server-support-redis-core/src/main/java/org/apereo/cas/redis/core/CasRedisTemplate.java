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
