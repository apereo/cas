package org.apereo.cas.adaptors.redis.services;

import org.apereo.cas.util.junit.DisabledIfContinuousIntegration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.TestPropertySource;
import redis.embedded.RedisSentinel;
import redis.embedded.RedisServer;

/**
 * Unit test for {@link RedisServiceRegistry} class.
 *
 * @author Julien Gribonvald
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "cas.serviceRegistry.redis.host=localhost",
    "cas.serviceRegistry.redis.port=6330",
    "cas.ticket.registry.redis.pool.max-active=20",
    "cas.ticket.registry.redis.sentinel.master=mymaster",
    "cas.ticket.registry.redis.sentinel.node[0]=localhost:26639",
    "cas.ticket.registry.redis.sentinel.node[1]=localhost:26640",
    "cas.ticket.registry.redis.sentinel.node[2]=localhost:26641"
})
@DisabledIfContinuousIntegration
public class SentinelEmbeddedServiceRegistryTests extends BaseRedisSentinelServiceRegistryTests {
    private static final RedisServer REDIS_SERVER_1;
    private static final RedisSentinel SENTINEL_SERVER_1;
    private static final RedisServer REDIS_SERVER_2;
    private static final RedisSentinel SENTINEL_SERVER_2;
    private static final RedisServer REDIS_SERVER_3;
    private static final RedisSentinel SENTINEL_SERVER_3;

    static {
        REDIS_SERVER_1 = RedisServer.builder().port(6330).build();
        SENTINEL_SERVER_1 =
                RedisSentinel.builder().masterName("mymaster").masterPort(6330).port(26639).quorumSize(2).build();
        REDIS_SERVER_2 =
                RedisServer.builder().port(6331).slaveOf("localhost", 6330).build();
        SENTINEL_SERVER_2 =
                RedisSentinel.builder().masterName("mymaster").masterPort(6320).port(26640).quorumSize(2).build();
        REDIS_SERVER_3 =
                RedisServer.builder().port(6332).slaveOf("localhost", 6330).build();
        SENTINEL_SERVER_3 =
                RedisSentinel.builder().masterName("mymaster").masterPort(6330).port(26641).quorumSize(2).build();
    }

    @BeforeAll
    @SneakyThrows
    public static void startRedis() throws Exception {
        REDIS_SERVER_1.start();
        SENTINEL_SERVER_1.start();
        REDIS_SERVER_2.start();
        SENTINEL_SERVER_2.start();
        REDIS_SERVER_3.start();
        SENTINEL_SERVER_3.start();
    }

    @AfterAll
    public static void stopRedis() {
        SENTINEL_SERVER_3.stop();
        SENTINEL_SERVER_2.stop();
        SENTINEL_SERVER_1.stop();
        REDIS_SERVER_3.stop();
        REDIS_SERVER_2.stop();
        REDIS_SERVER_1.stop();
    }
}
