package org.apereo.cas;

import org.apereo.cas.ticket.registry.RedisSentinelServerTicketRegistryTests;
import org.apereo.cas.ticket.registry.RedisServerTicketRegistryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllRedisTicketRegistryTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    RedisServerTicketRegistryTests.class,
    RedisSentinelServerTicketRegistryTests.class
})
@Suite
public class AllRedisTicketRegistryTestsSuite {
}
