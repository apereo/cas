package org.apereo.cas.redis.core.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This is {@link RedisUtils}.
 *
 * @author Fireborn Z
 * @since 6.5.0
 */
@UtilityClass
public class RedisUtils {
    private static final long SCAN_COUNT = 1000L;

    /**
     * Get redis keys using scan command.
     *
     * @param redisTemplate the redisTemplate
     * @param pattern       the redis keys pattern
     * @return the redis keys
     */
    public static Set<String> keys(final RedisTemplate<String, ?> redisTemplate, final String pattern) {
        return keys(redisTemplate, pattern, SCAN_COUNT);
    }

    /**
     * Get redis keys using scan command.
     *
     * @param redisTemplate the redisTemplate
     * @param pattern       the redis keys pattern
     * @param count         the scan limit
     * @return the redis keys
     */
    public static Set<String> keys(final RedisTemplate<String, ?> redisTemplate, final String pattern, final long count) {
        val cursor = Objects.requireNonNull(redisTemplate.getConnectionFactory())
            .getConnection()
            .scan(ScanOptions.scanOptions().match(pattern).count(count).build());
        return StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(cursor, Spliterator.ORDERED), false)
            .onClose(() -> IOUtils.closeQuietly(cursor))
            .map(key -> (String) redisTemplate.getKeySerializer().deserialize(key))
            .collect(Collectors.toSet());
    }
}
