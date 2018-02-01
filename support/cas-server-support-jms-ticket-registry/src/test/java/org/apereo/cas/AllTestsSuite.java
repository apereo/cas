package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.ticket.registry.queue.AddTicketMessageQueueCommandTests;
import org.apereo.cas.ticket.registry.queue.DeleteTicketMessageQueueCommandTests;
import org.apereo.cas.ticket.registry.queue.DeleteTicketsMessageQueueCommandTests;
import org.apereo.cas.ticket.registry.queue.UpdateTicketMessageQueueCommandTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({AddTicketMessageQueueCommandTests.class,
        DeleteTicketsMessageQueueCommandTests.class,
        DeleteTicketMessageQueueCommandTests.class,
        UpdateTicketMessageQueueCommandTests.class})
@Slf4j
public class AllTestsSuite {
}

