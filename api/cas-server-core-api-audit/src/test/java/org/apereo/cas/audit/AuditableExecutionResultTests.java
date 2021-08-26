package org.apereo.cas.audit;

import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AuditableExecutionResultTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Audits")
public class AuditableExecutionResultTests {

    @Test
    public void verifyOps() {
        val input = AuditableExecutionResult.builder()
            .exception(new IllegalArgumentException())
            .serviceTicket(mock(ServiceTicket.class))
            .ticketGrantingTicket(mock(TicketGrantingTicket.class))
            .authenticationResult(mock(AuthenticationResult.class))
            .build();

        assertTrue(input.getServiceTicket().isPresent());
        assertTrue(input.getTicketGrantingTicket().isPresent());
        assertTrue(input.getAuthenticationResult().isPresent());
        assertTrue(input.isExecutionFailure());
        assertThrows(IllegalArgumentException.class, input::throwExceptionIfNeeded);
    }
}
