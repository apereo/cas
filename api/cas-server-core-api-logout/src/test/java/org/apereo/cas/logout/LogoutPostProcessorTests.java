package org.apereo.cas.logout;

import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LogoutPostProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Logout")
public class LogoutPostProcessorTests {
    @Test
    public void verifyOperation() {
        val processor = new LogoutPostProcessor() {
            @Override
            public void handle(final TicketGrantingTicket ticketGrantingTicket) {
            }
        };
        assertNotNull(processor.getName());
        assertEquals(Ordered.HIGHEST_PRECEDENCE, processor.getOrder());
    }

}
