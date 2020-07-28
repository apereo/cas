package org.apereo.cas.ticket;

import org.apereo.cas.ticket.registry.DefaultEncodedTicket;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultEncodedTicketTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Tickets")
public class DefaultEncodedTicketTests {
    @Test
    public void verifyDefaults() {
        val id = UUID.randomUUID().toString();
        val ticket1 = new DefaultEncodedTicket(UUID.randomUUID().toString(),
            id, TicketGrantingTicket.PREFIX);
        val ticket2 = new DefaultEncodedTicket(UUID.randomUUID().toString(),
            id, TicketGrantingTicket.PREFIX);

        assertNull(ticket1.getTicketGrantingTicket());
        assertNull(ticket1.getCreationTime());
        assertNull(ticket1.getExpirationPolicy());
        assertEquals(0, ticket1.compareTo(ticket2));
        assertEquals(0, ticket1.getCountOfUses());
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                ticket1.markTicketExpired();
            }
        });
    }
}
