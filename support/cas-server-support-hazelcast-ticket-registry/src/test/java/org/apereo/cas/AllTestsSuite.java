package org.apereo.cas;

import org.apereo.cas.ticket.registry.DefaultHazelcastInstanceConfigurationTests;
import org.apereo.cas.ticket.registry.HazelcastTicketRegistryReplicationTests;
import org.apereo.cas.ticket.registry.HazelcastTicketRegistryTests;
import org.apereo.cas.ticket.registry.ProvidedHazelcastInstanceConfigurationTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({DefaultHazelcastInstanceConfigurationTests.class, 
        ProvidedHazelcastInstanceConfigurationTests.class,
        HazelcastTicketRegistryTests.class,
        HazelcastTicketRegistryReplicationTests.class})
public class AllTestsSuite {
}
