package org.apereo.cas.redis.core;

import module java.base;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

/**
 * This is {@link DefaultCasRedisTemplate}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class DefaultCasRedisTemplate<K, V> extends RedisTemplate<K, V> implements CasRedisTemplate<K, V> {
    /**
     * Find the keys that match the pattern with {@code SCAN}. The scan stream is closed when done, which closes the
     * cursor and returns its connection; with a connection pool, a connection that is never returned is lost to the pool.
     *
     * @param pattern the pattern
     * @return the keys
     */
    @Override
    public Set<K> keys(final K pattern) {
        try (val keys = scan(pattern.toString())) {
            return keys.collect(Collectors.toSet());
        }
    }

    @Override
    public Stream<K> scan(final String pattern, final Long count) {
        var scanOptions = ScanOptions.scanOptions().match(pattern);
        if (count != null && count > 0) {
            scanOptions = scanOptions.count(count);
        }
        val connection = Objects.requireNonNull(getConnectionFactory()).getConnection();
        val cursor = connection.keyCommands().scan(scanOptions.build());
        var resultingStream = StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(cursor, Spliterator.ORDERED), false)
            .onClose(() -> {
                IOUtils.closeQuietly(cursor);
                connection.close();
            })
            .map(key -> (K) getKeySerializer().deserialize(key))
            .distinct();
        if (count != null && count > 0) {
            resultingStream = resultingStream.limit(count);
        }
        return resultingStream;
    }

    /**
     * Count the keys that match the pattern with {@code SCAN}, closing the cursor and returning its connection when done.
     *
     * @param pattern the pattern
     * @return the number of keys
     */
    @Override
    public long count(final String pattern) {
        val scanOptions = ScanOptions.scanOptions().match(pattern);
        val connection = Objects.requireNonNull(getConnectionFactory()).getConnection();
        val cursor = connection.keyCommands().scan(scanOptions.build());
        try (val keys = StreamSupport.stream(Spliterators.spliteratorUnknownSize(cursor, Spliterator.ORDERED), false)
            .onClose(() -> {
                IOUtils.closeQuietly(cursor);
                connection.close();
            })) {
            return keys.count();
        }
    }
    
    @Override
    public void initialize() {
        afterPropertiesSet();
    }
}
