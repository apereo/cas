package org.apereo.cas.redis.core;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;

import java.util.stream.Stream;

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
    Stream<String> scan(String pattern, Long count);

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
