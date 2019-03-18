package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.RedisTicketRegistryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.util.junit.DisabledIfContinuousIntegration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import redis.embedded.RedisSentinel;
import redis.embedded.RedisServer;

/**
 * Unit test for {@link RedisTicketRegistry}.
 *
 * @author Julien Gribonvald
 * @since 6.1.0
 */
@Tag("Redis")
@SpringBootTest(classes = {
    RedisTicketRegistryConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreWebConfiguration.class,
    AopAutoConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class})
@TestPropertySource(properties = {
    "cas.ticket.registry.redis.host=localhost",
    "cas.ticket.registry.redis.port=6320",
    "cas.ticket.registry.redis.pool.max-active=20",
    "cas.ticket.registry.redis.sentinel.master=mymaster",
    "cas.ticket.registry.redis.sentinel.node[0]=localhost:26739",
    "cas.ticket.registry.redis.sentinel.node[1]=localhost:26740",
    "cas.ticket.registry.redis.sentinel.node[2]=localhost:26741"
})
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@DisabledIfContinuousIntegration
public class SentinelEmbeddedTicketRegistryTests extends BaseTicketRegistryTests {

    private static RedisServer REDIS_SERVER_1;
    private static RedisSentinel SENTINEL_SERVER_1;
    private static RedisServer REDIS_SERVER_2;
    private static RedisSentinel SENTINEL_SERVER_2;
    private static RedisServer REDIS_SERVER_3;
    private static RedisSentinel SENTINEL_SERVER_3;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @BeforeAll
    public static void startRedis() throws Exception {
        REDIS_SERVER_1 = RedisServer.builder().port(6320).build();
        REDIS_SERVER_1.start();
        SENTINEL_SERVER_1 =
                RedisSentinel.builder().masterName("mymaster").masterPort(6320).port(26739).quorumSize(2).build();
        SENTINEL_SERVER_1.start();

        REDIS_SERVER_2 = RedisServer.builder().port(6321).slaveOf("localhost", 6320).build();
        REDIS_SERVER_2.start();
        SENTINEL_SERVER_2 =
                RedisSentinel.builder().masterName("mymaster").masterPort(6320).port(26740).quorumSize(2).build();
        SENTINEL_SERVER_2.start();

        REDIS_SERVER_3 = RedisServer.builder().port(6322).slaveOf("localhost", 6320).build();
        REDIS_SERVER_3.start();
        SENTINEL_SERVER_3 =
                RedisSentinel.builder().masterName("mymaster").masterPort(6320).port(26741).quorumSize(2).build();
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

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return this.ticketRegistry;
    }
}
