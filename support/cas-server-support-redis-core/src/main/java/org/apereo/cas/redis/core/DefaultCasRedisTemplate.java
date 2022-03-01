package org.apereo.cas.redis.core;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This is {@link DefaultCasRedisTemplate}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class DefaultCasRedisTemplate<K, V> extends RedisTemplate<K, V> implements CasRedisTemplate<K, V> {
    @Override
    public Stream<String> keys(final String pattern, final long count) {
        var scanOptions = ScanOptions.scanOptions().match(pattern);
        if (count > 0) {
            scanOptions = scanOptions.count(count);
        }
        val cursor = getConnectionFactory().getConnection().scan(scanOptions.build());
        return StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(cursor, Spliterator.ORDERED), false)
            .onClose(() -> IOUtils.closeQuietly(cursor))
            .map(key -> (String) getKeySerializer().deserialize(key))
            .distinct();
    }

    @Override
    public void initialize() {
        afterPropertiesSet();
    }
}
