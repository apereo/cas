package org.apereo.cas.monitor;

import module java.base;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.spring.DirectObjectProvider;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link TicketRegistryHealthIndicatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Tickets")
class TicketRegistryHealthIndicatorTests {

    @Test
    void verifyUnknown() {
        val ticketRegistry = mock(TicketRegistry.class);
        when(ticketRegistry.sessionCount()).thenReturn(Long.valueOf(Integer.MIN_VALUE));
        when(ticketRegistry.serviceTicketCount()).thenReturn(Long.valueOf(Integer.MIN_VALUE));

        val indicator = new TicketRegistryHealthIndicator(new DirectObjectProvider<>(ticketRegistry), 1, 1);
        assertEquals(Status.UNKNOWN, indicator.health(true).getStatus());
    }

    @Test
    void verifyServiceTicketCount() {
        val ticketRegistry = mock(TicketRegistry.class);
        when(ticketRegistry.sessionCount()).thenReturn(1L);
        when(ticketRegistry.serviceTicketCount()).thenReturn(10L);

        val indicator = new TicketRegistryHealthIndicator(new DirectObjectProvider<>(ticketRegistry), 1, 1);
        val health = indicator.health(true);
        assertEquals(Health.status("WARN").build().getStatus(), health.getStatus());
    }
}
