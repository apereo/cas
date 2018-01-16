package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.ticket.registry.DefaultHazelcastInstanceConfigurationTests;
import org.apereo.cas.ticket.registry.HazelcastTicketRegistryTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({DefaultHazelcastInstanceConfigurationTests.class, HazelcastTicketRegistryTests.class})
@Slf4j
public class AllTestsSuite {
}
