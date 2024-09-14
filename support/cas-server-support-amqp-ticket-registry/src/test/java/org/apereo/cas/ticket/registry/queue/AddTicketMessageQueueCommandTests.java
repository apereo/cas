package org.apereo.cas.ticket.registry.queue;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.pubsub.commands.AddTicketMessageQueueCommand;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AddTicketMessageQueueCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnabledIfListeningOnPort(port = 5672)
@Tag("AMQP")
class AddTicketMessageQueueCommandTests extends AbstractTicketMessageQueueCommandTests {

    @Test
    void verifyAddTicket() throws Throwable {
        var ticket = new TicketGrantingTicketImpl("TGT", CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(ticket);
        val cmd = new AddTicketMessageQueueCommand(new PublisherIdentifier(UUID.randomUUID().toString()), ticket)
            .withPublisherIdentifier(new PublisherIdentifier());
        cmd.execute(ticketRegistry);
        ticket = ticketRegistry.getTicket(ticket.getId(), ticket.getClass());
        assertNotNull(ticket);
        assertEquals("TGT", ticket.getId());
    }
}
