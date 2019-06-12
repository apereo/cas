package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.junit.DisabledIfContinuousIntegration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.TestPropertySource;
import redis.embedded.RedisServer;

/**
 * Unit test for {@link RedisTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@TestPropertySource(properties = {
    "cas.ticket.registry.redis.host=localhost",
    "cas.ticket.registry.redis.port=6378",
    "cas.ticket.registry.redis.pool.max-active=20"
})
@DisabledIfContinuousIntegration
public class RedisEmbeddedTicketRegistryTests extends BaseRedisSentinelTicketRegistryTests {

    private static RedisServer REDIS_SERVER;

    @BeforeAll
    public static void startRedis() throws Exception {
        REDIS_SERVER = new RedisServer(6378);
        REDIS_SERVER.start();
    }

    @AfterAll
    public static void stopRedis() {
        REDIS_SERVER.stop();
    }

}
