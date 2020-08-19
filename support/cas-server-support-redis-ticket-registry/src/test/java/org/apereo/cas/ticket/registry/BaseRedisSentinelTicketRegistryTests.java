package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.RedisTicketRegistryConfiguration;
import org.apereo.cas.ticket.Ticket;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.RedisTemplate;
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
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Getter
public abstract class BaseRedisSentinelTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier("ticketRedisTemplate")
    protected RedisTemplate<String, Ticket> ticketRedisTemplate;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry newTicketRegistry;
}
