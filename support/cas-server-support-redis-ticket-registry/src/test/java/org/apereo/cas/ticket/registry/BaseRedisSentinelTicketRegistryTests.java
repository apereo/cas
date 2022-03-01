package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.RedisTicketRegistryConfiguration;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.ticket.Ticket;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Common class of Unit test for {@link RedisTicketRegistry} class.
 *
 * @author Julien Gribonvald
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    RedisTicketRegistryConfiguration.class,
    BaseTicketRegistryTests.SharedTestConfiguration.class
})
@EnableTransactionManagement
@EnableAspectJAutoProxy
@Getter
public abstract class BaseRedisSentinelTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier("ticketRedisTemplate")
    protected CasRedisTemplate<String, Ticket> ticketRedisTemplate;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;
}
