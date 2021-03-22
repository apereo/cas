package org.apereo.cas.ticket.registry;

import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;

import io.lettuce.core.ReadFrom;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

/**
 * Make sure Redis enumerations are still valid.
 * @author Hal Deadman
 * @since 6.4
 */
@Tag("Redis")
public class RedisPropertyValueTests {

    @Test
    public void validateRedisReadFromValues() {
        Stream.of(BaseRedisProperties.RedisReadFromTypes.values()).map(e -> e.name()).forEach(ReadFrom::valueOf);
    }
}
