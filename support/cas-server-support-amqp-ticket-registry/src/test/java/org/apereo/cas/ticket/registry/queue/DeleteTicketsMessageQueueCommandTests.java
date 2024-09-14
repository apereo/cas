package org.apereo.cas.ticket.registry.queue;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.pubsub.commands.DeleteTicketsMessageQueueCommand;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DeleteTicketsMessageQueueCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnabledIfListeningOnPort(port = 5672)
@Tag("AMQP")
class DeleteTicketsMessageQueueCommandTests extends AbstractTicketMessageQueueCommandTests {

    @Test
    void verifyDeleteTickets() throws Throwable {
        val ticket = new TicketGrantingTicketImpl("TGT", CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(ticket);
        val cmd = new DeleteTicketsMessageQueueCommand(new PublisherIdentifier(UUID.randomUUID().toString()))
            .withPublisherIdentifier(new PublisherIdentifier());
        cmd.execute(ticketRegistry);
        assertTrue(ticketRegistry.getTickets().isEmpty());
    }
}
