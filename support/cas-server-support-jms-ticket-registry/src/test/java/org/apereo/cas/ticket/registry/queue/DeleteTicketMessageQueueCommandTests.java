package org.apereo.cas.ticket.registry.queue;

import org.apereo.cas.StringBean;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * This is {@link DeleteTicketMessageQueueCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DeleteTicketMessageQueueCommandTests extends AbstractTicketMessageQueueCommandTests {

    @Test
    public void verifyDeleteTicket() {
        val ticket = new TicketGrantingTicketImpl("TGT", CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        ticketRegistry.addTicket(ticket);
        val cmd = new DeleteTicketMessageQueueCommand(new StringBean(), ticket.getId());
        cmd.execute(ticketRegistry);
        assertTrue(ticketRegistry.getTickets().isEmpty());
    }
}
