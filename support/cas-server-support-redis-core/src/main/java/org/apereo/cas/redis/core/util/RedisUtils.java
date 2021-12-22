package org.apereo.cas.redis.core.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This is {@link RedisUtils}.
 *
 * @author Fireborn Z
 * @since 6.5.0
 */
@UtilityClass
public class RedisUtils {
    /**
     * Get redis keys using scan command.
     *
     * @param redisTemplate the redisTemplate
     * @param pattern       the redis keys pattern
     * @param count         the scan count
     * @return the redis keys
     */
    public static Stream<String> keys(final RedisTemplate<String, ?> redisTemplate, final String pattern, final long count) {
        var scanOptions = ScanOptions.scanOptions().match(pattern);
        if (count > 0) {
            scanOptions = scanOptions.count(count);
        }
        val cursor = Objects.requireNonNull(redisTemplate.getConnectionFactory())
            .getConnection()
            .scan(scanOptions.build());
        return StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(cursor, Spliterator.ORDERED), false)
            .onClose(() -> IOUtils.closeQuietly(cursor))
            .map(key -> (String) redisTemplate.getKeySerializer().deserialize(key))
            .distinct();
    }
}
