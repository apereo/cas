package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.config.CasRedisCoreAutoConfiguration;
import org.apereo.cas.config.CasRedisTicketRegistryAutoConfiguration;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.ticket.registry.RedisTicketRegistry.CasRedisTemplates;
import lombok.Getter;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * Common class of Unit test for {@link RedisTicketRegistry} class.
 *
 * @author Julien Gribonvald
 * @since 6.1.0
 */
@ImportAutoConfiguration({
    CasRedisCoreAutoConfiguration.class,
    CasRedisTicketRegistryAutoConfiguration.class
})
@EnableTransactionManagement(proxyTargetClass = false)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@Tag("SkipClearingTicketRegistry")
@Getter
public abstract class BaseRedisSentinelTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier("ticketRedisTemplate")
    protected CasRedisTemplate<String, RedisTicketDocument> ticketRedisTemplate;

    @Autowired
    @Qualifier("redisHealthIndicator")
    protected HealthIndicator redisHealthIndicator;
    
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @Autowired
    @Qualifier("casRedisTemplates")
    private CasRedisTemplates casRedisTemplates;

    @Override
    protected boolean isCipherExecutorOwnedByContext() {
        return true;
    }

    /**
     * Whether this class has a Redis keyspace to itself and runs its methods one at a time, which is
     * what the tests that empty or count the whole registry need. Classes sharing a keyspace skip them.
     *
     * @return true if the keyspace is not shared
     */
    protected boolean isRegistryIsolated() {
        return false;
    }

    @Override
    @RepeatedTest(2)
    void verifyGetTicketsIsZero() throws Throwable {
        assumeTrue(isRegistryIsolated());
        super.verifyGetTicketsIsZero();
    }

    @Override
    @RepeatedTest(2)
    void verifyDeleteAllExistingTickets() throws Throwable {
        assumeTrue(isRegistryIsolated());
        super.verifyDeleteAllExistingTickets();
    }
}
