package org.apereo.cas.ticket.registry.queue;

import org.apereo.cas.JmsQueueIdentifier;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DeleteTicketsMessageQueueCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnabledIfPortOpen(port = 61616)
@Tag("JMS")
public class DeleteTicketsMessageQueueCommandTests extends AbstractTicketMessageQueueCommandTests {

    @Test
    public void verifyDeleteTickets() {
        val ticket = new TicketGrantingTicketImpl("TGT", CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.getObject().addTicket(ticket);
        val cmd = new DeleteTicketsMessageQueueCommand(new JmsQueueIdentifier());
        cmd.execute(ticketRegistry.getObject());
        assertTrue(ticketRegistry.getObject().getTickets().isEmpty());
    }
}
