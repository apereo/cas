package org.apereo.cas.ticket.registry;

import org.apereo.cas.category.RedisCategory;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.RedisTicketRegistryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;

import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import redis.embedded.RedisServer;

import java.util.Arrays;
import java.util.Collection;

/**
 * Unit test for {@link RedisTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(Parameterized.class)
@Category(RedisCategory.class)
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
    "cas.ticket.registry.redis.port=6378",
    "cas.ticket.registry.redis.pool.max-active=20"
})
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class RedisEmbeddedTicketRegistryTests extends BaseSpringRunnableTicketRegistryTests {

    private static RedisServer REDIS_SERVER;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    public RedisEmbeddedTicketRegistryTests(final boolean useEncryption) {
        super(useEncryption);
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() {
        return Arrays.asList(false, true);
    }


    @BeforeAll
    public static void startRedis() throws Exception {
        REDIS_SERVER = new RedisServer(6378);
        REDIS_SERVER.start();
    }

    @AfterAll
    public static void stopRedis() {
        REDIS_SERVER.stop();
    }

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return this.ticketRegistry;
    }
}
