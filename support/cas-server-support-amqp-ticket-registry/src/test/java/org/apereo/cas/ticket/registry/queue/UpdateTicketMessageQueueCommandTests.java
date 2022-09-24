package org.apereo.cas.ticket.registry.queue;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.queue.commands.UpdateTicketMessageQueueCommand;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UpdateTicketMessageQueueCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnabledIfListeningOnPort(port = 5672)
@Tag("AMQP")
public class UpdateTicketMessageQueueCommandTests extends AbstractTicketMessageQueueCommandTests {

    @Test
    public void verifyUpdateTicket() throws Exception {
        var ticket = new TicketGrantingTicketImpl("TGT",
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val cmd = new UpdateTicketMessageQueueCommand(new PublisherIdentifier(), ticket).withId(new PublisherIdentifier());
        cmd.execute(ticketRegistry);
        ticket = ticketRegistry.getTicket(ticket.getId(), ticket.getClass());
        assertNotNull(ticket);
        assertEquals("TGT", ticket.getId());
    }
}
