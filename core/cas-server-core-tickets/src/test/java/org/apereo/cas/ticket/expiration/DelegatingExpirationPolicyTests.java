package org.apereo.cas.ticket.expiration;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.TicketGrantingTicketAwareTicket;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatingExpirationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Tickets")
class DelegatingExpirationPolicyTests {

    @Test
    void verifyOperation() {
        val policy = new BaseDelegatingExpirationPolicy() {
            @Serial
            private static final long serialVersionUID = -5896270899735612574L;

            @Override
            protected String getExpirationPolicyNameFor(final AuthenticationAwareTicket ticketState) {
                if ("expired".equals(ticketState.getAuthentication().getPrincipal().getId())) {
                    return AlwaysExpiresExpirationPolicy.class.getSimpleName();
                }
                return POLICY_NAME_DEFAULT;
            }
        };
        policy.addPolicy(BaseDelegatingExpirationPolicy.POLICY_NAME_DEFAULT, AlwaysExpiresExpirationPolicy.INSTANCE);
        policy.addPolicy(new NeverExpiresExpirationPolicy());

        var ticketState = mock(TicketGrantingTicketAwareTicket.class);
        when(ticketState.getAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication("cas"));
        assertTrue(policy.isExpired(ticketState));
        assertEquals(0, (long) policy.getTimeToLive(ticketState));
        assertEquals(0, (long) policy.getTimeToLive());

        ticketState = mock(TicketGrantingTicketAwareTicket.class);
        when(ticketState.getAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication("expired"));
        assertFalse(policy.isExpired(ticketState));
        assertEquals(0, (long) policy.getTimeToLive(ticketState));
        assertNotNull(policy.toString());
    }
}
