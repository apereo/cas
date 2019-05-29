package org.apereo.cas.ticket.registry;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
@RunWith(JUnitPlatform.class)
public class AllRedisTicketRegistryTestsSuite {
}
