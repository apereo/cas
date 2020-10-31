package org.apereo.cas.ticket;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link TicketGrantingTicketTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Tickets")
public class TicketGrantingTicketTests {
    @Test
    public void verifyOperation() {
        val ticket = mock(TicketGrantingTicket.class);
        when(ticket.getDescendantTickets()).thenCallRealMethod();
        assertTrue(ticket.getDescendantTickets().isEmpty());
    }

}
