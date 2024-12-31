package org.apereo.cas.ticket.registry.queue;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.pubsub.commands.DeleteTicketMessageQueueCommand;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DeleteTicketMessageQueueCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnabledIfListeningOnPort(port = 5672)
@Tag("AMQP")
class DeleteTicketMessageQueueCommandTests extends AbstractTicketMessageQueueCommandTests {

    @BeforeEach
    void setup() {
        ticketRegistry.deleteAll();
    }

    @Test
    void verifyDeleteTicket() throws Throwable {
        val ticket = new TicketGrantingTicketImpl("TGT-665500",
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(ticket);
        val cmd = new DeleteTicketMessageQueueCommand(new PublisherIdentifier(), ticket.getId())
            .withPublisherIdentifier(new PublisherIdentifier());
        cmd.execute(ticketRegistry);
        assertNull(ticketRegistry.getTicket(ticket.getId()));
    }
}
