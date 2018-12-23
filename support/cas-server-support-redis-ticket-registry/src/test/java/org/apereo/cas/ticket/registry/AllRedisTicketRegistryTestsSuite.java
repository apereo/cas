package org.apereo.cas.ticket.registry;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllRedisTicketRegistryTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    RedisEmbeddedTicketRegistryTests.class,
    RedisServerTicketRegistryTests.class
})
public class AllRedisTicketRegistryTestsSuite {
}
