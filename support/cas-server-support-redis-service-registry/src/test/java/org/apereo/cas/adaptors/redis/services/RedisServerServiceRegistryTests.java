package org.apereo.cas.adaptors.redis.services;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link RedisServiceRegistry} class.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@TestPropertySource(properties = {
    "cas.service-registry.redis.host=localhost",
    "cas.service-registry.redis.port=6379",
    "cas.service-registry.redis.database=1"
})
@EnabledIfListeningOnPort(port = 6379)
@Tag("Redis")
class RedisServerServiceRegistryTests extends BaseRedisSentinelServiceRegistryTests {
}
