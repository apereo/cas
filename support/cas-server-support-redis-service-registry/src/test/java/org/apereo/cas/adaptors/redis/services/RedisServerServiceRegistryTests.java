package org.apereo.cas.adaptors.redis.services;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link RedisServiceRegistry} class.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@TestPropertySource(properties = {
    "cas.serviceRegistry.redis.host=localhost",
    "cas.serviceRegistry.redis.port=6379"
})
@EnabledIfPortOpen(port = 6379)
public class RedisServerServiceRegistryTests extends BaseRedisSentinelServiceRegistryTests {
}
