package org.apereo.cas.ticket;

import org.apereo.cas.ticket.registry.DefaultEncodedTicket;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultEncodedTicketTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Tickets")
class DefaultEncodedTicketTests {
    @Test
    void verifyDefaults() {
        val id = UUID.randomUUID().toString();
        val ticket1 = new DefaultEncodedTicket(UUID.randomUUID().toString(),
            id, TicketGrantingTicket.PREFIX);
        ticket1.markTicketStateless();
        assertTrue(ticket1.isStateless());
        val ticket2 = new DefaultEncodedTicket(UUID.randomUUID().toString(),
            id, TicketGrantingTicket.PREFIX);
        ticket2.markTicketStateless();
        assertTrue(ticket2.isStateless());

        assertNull(ticket1.getCreationTime());
        assertNull(ticket1.getExpirationPolicy());
        assertEquals(0, ticket1.compareTo(ticket2));
        assertEquals(0, ticket1.getCountOfUses());
        assertDoesNotThrow(ticket1::markTicketExpired);
    }
}
