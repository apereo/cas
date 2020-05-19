package org.apereo.cas.adaptors.redis.services;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link RedisServiceRegistry} class.
 *
 * @author Julien Gribonvald
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "cas.service-registry.redis.host=localhost",
    "cas.service-registry.redis.port=6379",
    "cas.ticket.registry.redis.pool.max-active=20",
    "cas.ticket.registry.redis.pool.enabled=true",
    "cas.ticket.registry.redis.sentinel.master=mymaster",
    "cas.ticket.registry.redis.sentinel.node[0]=localhost:26379",
    "cas.ticket.registry.redis.sentinel.node[1]=localhost:26380",
    "cas.ticket.registry.redis.sentinel.node[2]=localhost:26381"
})
@EnabledIfPortOpen(port = 6379)
@Tag("Redis")
public class SentinelServerServiceRegistryTests extends BaseRedisSentinelServiceRegistryTests {
}
