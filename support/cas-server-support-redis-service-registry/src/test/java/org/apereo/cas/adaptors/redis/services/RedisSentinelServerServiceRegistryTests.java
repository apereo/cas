package org.apereo.cas.adaptors.redis.services;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestMethodOrder;
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
    "cas.service-registry.redis.share-native-connections=true",

    "cas.service-registry.redis.sentinel.master=mymaster",
    "cas.service-registry.redis.sentinel.password=password456",
    "cas.service-registry.redis.sentinel.node[0]=localhost:26379",
    "cas.service-registry.redis.sentinel.node[1]=localhost:26380",
    "cas.service-registry.redis.sentinel.node[2]=localhost:26381",
    "cas.service-registry.redis.timeout=5000"
})
@EnabledIfListeningOnPort(port = 6379)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("Redis")
class RedisSentinelServerServiceRegistryTests extends BaseRedisSentinelServiceRegistryTests {
}
