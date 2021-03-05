package org.apereo.cas.ticket;

import org.apereo.cas.ticket.registry.TicketRegistryCleaner;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link TicketRegistryCleanerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Tickets")
public class TicketRegistryCleanerTests {

    @Test
    public void verifyOperation() {
        val cleaner = mock(TicketRegistryCleaner.class);
        when(cleaner.clean()).thenCallRealMethod();
        when(cleaner.cleanTicket(any())).thenCallRealMethod();
        assertEquals(0, cleaner.clean());
        assertEquals(0, cleaner.cleanTicket(mock(Ticket.class)));
    }

}
