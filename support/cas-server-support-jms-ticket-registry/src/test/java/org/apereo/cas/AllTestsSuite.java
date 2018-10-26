package org.apereo.cas;

import org.apereo.cas.ticket.registry.JmsTicketRegistryTests;
import org.apereo.cas.ticket.registry.queue.AddTicketMessageQueueCommandTests;
import org.apereo.cas.ticket.registry.queue.DeleteTicketMessageQueueCommandTests;
import org.apereo.cas.ticket.registry.queue.DeleteTicketsMessageQueueCommandTests;
import org.apereo.cas.ticket.registry.queue.UpdateTicketMessageQueueCommandTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SelectClasses({
    AddTicketMessageQueueCommandTests.class,
    DeleteTicketsMessageQueueCommandTests.class,
    DeleteTicketMessageQueueCommandTests.class,
    UpdateTicketMessageQueueCommandTests.class,
    JmsTicketRegistryTests.class})
public class AllTestsSuite {
}

