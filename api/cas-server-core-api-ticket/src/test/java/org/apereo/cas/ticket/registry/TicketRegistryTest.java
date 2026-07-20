package org.apereo.cas.ticket.registry;

import module java.base;
import lombok.val;
import org.apereo.cas.ticket.Ticket;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link TicketRegistryTest}.
 *
 * @author Giorgi Chapidze
 * @since 8.1.0
 */
@Tag("Tickets")
class TicketRegistryTest {

    @Test
    void verifyAddTicketStreamSkipsNullResultsViaMapMulti() throws Throwable {
        val keep = mock(Ticket.class);
        when(keep.getId()).thenReturn("TGT-keep");

        val drop = mock(Ticket.class);
        when(drop.getId()).thenReturn("TGT-drop");

        val registry = mock(TicketRegistry.class);
        when(registry.addTicket(any(Ticket.class))).thenAnswer(invocation -> {
            val ticket = invocation.<Ticket>getArgument(0);
            return "TGT-drop".equals(ticket.getId()) ? null : ticket;
        });
        when(registry.addTicket(ArgumentMatchers.<Stream<? extends Ticket>>any())).thenCallRealMethod();

        val result = registry.addTicket(Stream.of(keep, drop));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TGT-keep", result.getFirst().getId());
    }

    @Test
    void verifyAddTicketStreamKeepsAllValidResults() throws Throwable {
        val tgt1 = mock(Ticket.class);
        when(tgt1.getId()).thenReturn("TGT-1");

        val tgt2 = mock(Ticket.class);
        when(tgt2.getId()).thenReturn("TGT-2");

        val registry = mock(TicketRegistry.class);
        when(registry.addTicket(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(registry.addTicket(ArgumentMatchers.<Stream<? extends Ticket>>any())).thenCallRealMethod();

        val result = registry.addTicket(Stream.of(tgt1, tgt2));

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void verifyAddTicketStreamReturnsEmptyListWhenAllRejected() throws Throwable {
        val tgt1 = mock(Ticket.class);
        val tgt2 = mock(Ticket.class);

        val registry = mock(TicketRegistry.class);
        when(registry.addTicket(any(Ticket.class))).thenReturn(null);
        when(registry.addTicket(ArgumentMatchers.<Stream<? extends Ticket>>any())).thenCallRealMethod();

        val result = registry.addTicket(Stream.of(tgt1, tgt2));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
