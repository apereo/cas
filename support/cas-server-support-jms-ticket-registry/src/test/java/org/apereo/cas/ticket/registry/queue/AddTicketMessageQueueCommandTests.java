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
 * This is {@link AddTicketMessageQueueCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnabledIfPortOpen(port = 61616)
@Tag("JMS")
public class AddTicketMessageQueueCommandTests extends AbstractTicketMessageQueueCommandTests {

    @Test
    public void verifyAddTicket() {
        var ticket = new TicketGrantingTicketImpl("TGT", CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.getObject().addTicket(ticket);
        val cmd = new AddTicketMessageQueueCommand(new JmsQueueIdentifier(), ticket);
        cmd.execute(ticketRegistry.getObject());
        ticket = ticketRegistry.getObject().getTicket(ticket.getId(), ticket.getClass());
        assertNotNull(ticket);
        assertEquals("TGT", ticket.getId());
    }
}
