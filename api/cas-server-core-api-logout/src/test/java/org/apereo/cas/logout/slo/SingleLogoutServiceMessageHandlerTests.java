package org.apereo.cas.logout.slo;

import org.apereo.cas.authentication.principal.WebApplicationService;

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
@Tag("Logout")
class SingleLogoutServiceMessageHandlerTests {

    @Test
    void verifyOperation() throws Throwable {
        val handler = new SingleLogoutServiceMessageHandler() {
            @Override
            public Collection<SingleLogoutRequestContext> handle(final WebApplicationService singleLogoutService,
                                                                 final String ticketId,
                                                                 final SingleLogoutExecutionRequest context) {
                return List.of();
            }

            @Override
            public boolean performBackChannelLogout(final SingleLogoutRequestContext request) {
                return false;
            }

            @Override
            public SingleLogoutMessage createSingleLogoutMessage(final SingleLogoutRequestContext logoutRequest) {
                return null;
            }
        };
        assertEquals(Ordered.LOWEST_PRECEDENCE, handler.getOrder());
        assertTrue(handler.supports(SingleLogoutExecutionRequest.builder().build(), mock(WebApplicationService.class)));

    }

}
