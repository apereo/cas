package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.junit.DisabledIfContinuousIntegration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.TestPropertySource;
import redis.embedded.RedisSentinel;
import redis.embedded.RedisServer;

/**
 * Unit test for {@link RedisTicketRegistry}.
 *
 * @author Julien Gribonvald
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "cas.ticket.registry.redis.host=localhost",
    "cas.ticket.registry.redis.port=6320",
    "cas.ticket.registry.redis.readFrom=MASTER",
    "cas.ticket.registry.redis.pool.max-active=20",
    "cas.ticket.registry.redis.sentinel.master=mymaster",
    "cas.ticket.registry.redis.sentinel.node[0]=localhost:26739",
    "cas.ticket.registry.redis.sentinel.node[1]=localhost:26740",
    "cas.ticket.registry.redis.sentinel.node[2]=localhost:26741"
})
@DisabledIfContinuousIntegration
public class SentinelEmbeddedTicketRegistryTests extends BaseRedisSentinelTicketRegistryTests {

    private static final RedisServer REDIS_SERVER_1;
    private static final RedisSentinel SENTINEL_SERVER_1;
    private static final RedisServer REDIS_SERVER_2;
    private static final RedisSentinel SENTINEL_SERVER_2;
    private static final RedisServer REDIS_SERVER_3;
    private static final RedisSentinel SENTINEL_SERVER_3;

    static {
        REDIS_SERVER_1 = RedisServer.builder().port(6320).build();
        SENTINEL_SERVER_1 =
                RedisSentinel.builder().masterName("mymaster").masterPort(6320).port(26739).quorumSize(2).build();
        REDIS_SERVER_2 =
                RedisServer.builder().port(6321).slaveOf("localhost", 6320).build();
        SENTINEL_SERVER_2 =
                RedisSentinel.builder().masterName("mymaster").masterPort(6320).port(26740).quorumSize(2).build();
        REDIS_SERVER_3 =
                RedisServer.builder().port(6322).slaveOf("localhost", 6320).build();
        SENTINEL_SERVER_3=
                RedisSentinel.builder().masterName("mymaster").masterPort(6320).port(26741).quorumSize(2).build();
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
