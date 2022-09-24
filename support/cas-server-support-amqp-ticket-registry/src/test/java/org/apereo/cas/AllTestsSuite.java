package org.apereo.cas;

import org.apereo.cas.ticket.registry.AMQPDefaultTicketRegistryTests;
import org.apereo.cas.ticket.registry.AMQPTicketRegistryQueueReceiverTests;
import org.apereo.cas.ticket.registry.queue.AddTicketMessageQueueCommandTests;
import org.apereo.cas.ticket.registry.queue.DeleteTicketMessageQueueCommandTests;
import org.apereo.cas.ticket.registry.queue.DeleteTicketsMessageQueueCommandTests;
import org.apereo.cas.ticket.registry.queue.UpdateTicketMessageQueueCommandTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

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
    AMQPTicketRegistryQueueReceiverTests.class,
    AMQPDefaultTicketRegistryTests.class
})
@Suite
public class AllTestsSuite {
}

