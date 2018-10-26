package org.apereo.cas;

import org.apereo.cas.ticket.registry.DefaultHazelcastInstanceConfigurationTests;
import org.apereo.cas.ticket.registry.HazelcastTicketRegistryTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@SelectClasses({DefaultHazelcastInstanceConfigurationTests.class, HazelcastTicketRegistryTests.class})
public class AllTestsSuite {
}
