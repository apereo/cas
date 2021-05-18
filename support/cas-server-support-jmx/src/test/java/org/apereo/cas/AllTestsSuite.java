package org.apereo.cas;

import org.apereo.cas.config.CasJmxConfigurationTests;
import org.apereo.cas.jmx.services.ServicesManagerManagedResourceTests;
import org.apereo.cas.jmx.ticket.TicketRegistryManagedResourceTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SelectClasses({
    CasJmxConfigurationTests.class,
    TicketRegistryManagedResourceTests.class,
    ServicesManagerManagedResourceTests.class
})
@Suite
public class AllTestsSuite {
}

