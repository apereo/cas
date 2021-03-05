package org.apereo.cas.monitor;

import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link TicketRegistryHealthIndicatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Tickets")
public class TicketRegistryHealthIndicatorTests {

    @Test
    public void verifyUnknown() {
        val ticketRegistry = mock(TicketRegistry.class);
        when(ticketRegistry.sessionCount()).thenReturn(Long.valueOf(Integer.MIN_VALUE));
        when(ticketRegistry.serviceTicketCount()).thenReturn(Long.valueOf(Integer.MIN_VALUE));

        val indicator = new TicketRegistryHealthIndicator(ticketRegistry, 1, 1);
        assertEquals(Status.UNKNOWN, indicator.getHealth(true).getStatus());
    }

    @Test
    public void verifyServiceTicketCount() {
        val ticketRegistry = mock(TicketRegistry.class);
        when(ticketRegistry.sessionCount()).thenReturn(1L);
        when(ticketRegistry.serviceTicketCount()).thenReturn(10L);

        val indicator = new TicketRegistryHealthIndicator(ticketRegistry, 1, 1);
        val health = indicator.getHealth(true);
        assertEquals(Health.status("WARN").build().getStatus(), health.getStatus());
    }
}
