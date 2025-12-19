package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.config.CasRedisCoreAutoConfiguration;
import org.apereo.cas.config.CasRedisTicketRegistryAutoConfiguration;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.ticket.registry.RedisTicketRegistry.CasRedisTemplates;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

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

}
