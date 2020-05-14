package org.apereo.cas.services;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.ticket.TicketState;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ChainingRegisteredServiceSingleSignOnParticipationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Simple")
public class ChainingRegisteredServiceSingleSignOnParticipationPolicyTests {
    @Test
    public void verifySsoParticipationByAuthenticationDateFails() {
        val authn = mock(Authentication.class);
        when(authn.getAuthenticationDate()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(10));

        val state = mock(TicketState.class);
        when(state.getAuthentication()).thenReturn(authn);
        val chain = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        chain.addPolicy(new AuthenticationDateRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 1, 0));

        assertFalse(chain.shouldParticipateInSso(state));
    }

    @Test
    public void verifySsoParticipationByAuthenticationDatePasses() {
        val authn = mock(Authentication.class);
        when(authn.getAuthenticationDate()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(5));

        val state = mock(TicketState.class);
        when(state.getAuthentication()).thenReturn(authn);
        val chain = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        chain.addPolicy(new AuthenticationDateRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 10, 0));

        assertTrue(chain.shouldParticipateInSso(state));
    }

    @Test
    public void verifySsoParticipationByLastUsedTimeFails() {
        val state = mock(TicketState.class);
        when(state.getLastTimeUsed()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(10));
        val chain = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        chain.addPolicy(new LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 1, 0));

        assertFalse(chain.shouldParticipateInSso(state));
    }

    @Test
    public void verifySsoParticipationByLastUsedTimePasses() {
        val state = mock(TicketState.class);
        when(state.getLastTimeUsed()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(5));
        val chain = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        chain.addPolicy(new LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 10, 0));

        assertTrue(chain.shouldParticipateInSso(state));
    }
}
