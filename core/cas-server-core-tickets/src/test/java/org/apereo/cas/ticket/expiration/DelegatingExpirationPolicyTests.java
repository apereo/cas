package org.apereo.cas.ticket.expiration;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.TicketState;

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
public class DelegatingExpirationPolicyTests {

    @Test
    public void verifyOperation() {
        val policy = new BaseDelegatingExpirationPolicy() {
            private static final long serialVersionUID = -5896270899735612574L;

            @Override
            protected String getExpirationPolicyNameFor(final TicketState ticketState) {
                if (ticketState.getAuthentication().getPrincipal().getId().equals("expired")) {
                    return AlwaysExpiresExpirationPolicy.class.getSimpleName();
                }
                return POLICY_NAME_DEFAULT;
            }
        };
        policy.addPolicy(BaseDelegatingExpirationPolicy.POLICY_NAME_DEFAULT, new AlwaysExpiresExpirationPolicy());
        policy.addPolicy(new NeverExpiresExpirationPolicy());

        var ticketState = mock(TicketState.class);
        when(ticketState.getAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication("cas"));
        assertTrue(policy.isExpired(ticketState));
        assertEquals((long) policy.getTimeToLive(ticketState), 0);
        assertEquals((long) policy.getTimeToLive(), 0);
        assertEquals((long) policy.getTimeToIdle(), 0);

        ticketState = mock(TicketState.class);
        when(ticketState.getAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication("expired"));
        assertFalse(policy.isExpired(ticketState));
        assertEquals((long) policy.getTimeToLive(ticketState), 0);
        assertNotNull(policy.toString());
    }
}
