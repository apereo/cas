package org.apereo.cas.adaptors.redis.services;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link RedisServiceRegistry} class.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@TestPropertySource(properties = {
    "cas.service-registry.redis.host=localhost",
    "cas.service-registry.redis.port=6379"
})
@EnabledIfPortOpen(port = 6379)
@Tag("Redis")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RedisServerServiceRegistryTests extends BaseRedisSentinelServiceRegistryTests {
}
