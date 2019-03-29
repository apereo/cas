package org.apereo.cas.ticket.registry;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllRedisTicketRegistryTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    RedisEmbeddedTicketRegistryTests.class,
    SentinelEmbeddedTicketRegistryTests.class,
    RedisServerTicketRegistryTests.class,
    SentinelServerTicketRegistryTests.class
})
public class AllRedisTicketRegistryTestsSuite {
}
