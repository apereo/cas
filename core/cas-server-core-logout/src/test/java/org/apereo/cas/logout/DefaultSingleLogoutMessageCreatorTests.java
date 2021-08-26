package org.apereo.cas.logout;

import org.apereo.cas.logout.slo.SingleLogoutRequestContext;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceLogoutType;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultSingleLogoutMessageCreatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Logout")
public class DefaultSingleLogoutMessageCreatorTests {
    @Test
    public void verifyFrontChannel() {
        val input = new DefaultSingleLogoutMessageCreator();

        val exec = SingleLogoutExecutionRequest.builder()
            .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
            .httpServletRequest(Optional.of(new MockHttpServletRequest()))
            .build();

        val request = mock(SingleLogoutRequestContext.class);
        when(request.getExecutionRequest()).thenReturn(exec);
        when(request.getTicketId()).thenReturn(UUID.randomUUID().toString());
        when(request.getLogoutType()).thenReturn(RegisteredServiceLogoutType.FRONT_CHANNEL);
        assertNotNull(input.create(request));
    }

}
