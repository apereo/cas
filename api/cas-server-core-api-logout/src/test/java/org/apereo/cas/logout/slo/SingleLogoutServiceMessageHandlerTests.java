package org.apereo.cas.logout.slo;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SingleLogoutServiceMessageHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
public class SingleLogoutServiceMessageHandlerTests {

    @Test
    public void verifyOperation() {
        val handler = new SingleLogoutServiceMessageHandler() {
            @Override
            public Collection<SingleLogoutRequest> handle(final WebApplicationService singleLogoutService,
                                                          final String ticketId,
                                                          final TicketGrantingTicket ticketGrantingTicket) {
                return List.of();
            }

            @Override
            public boolean performBackChannelLogout(final SingleLogoutRequest request) {
                return false;
            }

            @Override
            public SingleLogoutMessage createSingleLogoutMessage(final SingleLogoutRequest logoutRequest) {
                return null;
            }
        };
        assertEquals(Ordered.LOWEST_PRECEDENCE, handler.getOrder());
        assertTrue(handler.supports(mock(WebApplicationService.class)));

    }

}
