package org.apereo.cas.adaptors.redis.services;

import org.apereo.cas.util.junit.DisabledIfContinuousIntegration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.TestPropertySource;
import redis.embedded.RedisServer;

/**
 * Unit test for {@link RedisServiceRegistry} class.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@TestPropertySource(properties = {
    "cas.serviceRegistry.redis.host=localhost",
    "cas.serviceRegistry.redis.port=6380"
})
@DisabledIfContinuousIntegration
public class RedisEmbeddedServiceRegistryTests extends BaseRedisSentinelServiceRegistryTests {
    private static RedisServer REDIS_SERVER;

    @BeforeAll
    @SneakyThrows
    public static void startRedis() {
        REDIS_SERVER = new RedisServer(6380);
        REDIS_SERVER.start();
    }

    @AfterAll
    public static void stopRedis() {
        REDIS_SERVER.stop();
    }

}
