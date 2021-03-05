package org.apereo.cas;

import org.apereo.cas.ticket.TicketDefinitionTests;
import org.apereo.cas.ticket.TicketGrantingTicketTests;
import org.apereo.cas.ticket.TicketRegistryCleanerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    TicketDefinitionTests.class,
    TicketRegistryCleanerTests.class,
    TicketGrantingTicketTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
