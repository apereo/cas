package org.apereo.cas;

import org.apereo.cas.ticket.registry.DefaultHazelcastInstanceConfigurationTests;
import org.apereo.cas.ticket.registry.HazelcastTicketRegistryTests;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(Enclosed.class)
@Suite.SuiteClasses({DefaultHazelcastInstanceConfigurationTests.class, HazelcastTicketRegistryTests.class})
public class AllTestsSuite {
}
