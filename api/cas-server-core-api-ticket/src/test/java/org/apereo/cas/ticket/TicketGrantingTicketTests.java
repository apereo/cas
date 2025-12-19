package org.apereo.cas.ticket;

import module java.base;
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
class TicketGrantingTicketTests {
    @Test
    void verifyOperation() {
        val ticket = mock(TicketGrantingTicket.class);
        when(ticket.getDescendantTickets()).thenCallRealMethod();
        assertTrue(ticket.getDescendantTickets().isEmpty());
    }

}
