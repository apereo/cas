package org.apereo.cas.services;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.util.model.TriStateBoolean;

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
@Tag("RegisteredService")
public class ChainingRegisteredServiceSingleSignOnParticipationPolicyTests {

    @Test
    public void verifyOperation() {
        val input = mock(RegisteredServiceSingleSignOnParticipationPolicy.class);
        when(input.getOrder()).thenCallRealMethod();
        when(input.getCreateCookieOnRenewedAuthentication()).thenCallRealMethod();
        assertEquals(0, input.getOrder());
        assertEquals(TriStateBoolean.UNDEFINED, input.getCreateCookieOnRenewedAuthentication());
    }

    
    @Test
    public void verifySsoParticipationByAuthenticationDateFails() {
        val authn = mock(Authentication.class);
        when(authn.getAuthenticationDate()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(10));

        val state = mock(TicketState.class);
        when(state.getAuthentication()).thenReturn(authn);
        val chain = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        chain.addPolicy(new AuthenticationDateRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 1, 0));

        assertFalse(chain.shouldParticipateInSso(RegisteredServiceTestUtils.getRegisteredService(), state));
    }

    @Test
    public void verifySsoParticipationByAuthenticationDatePasses() {
        val authn = mock(Authentication.class);
        when(authn.getAuthenticationDate()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(5));

        val state = mock(TicketState.class);
        when(state.getAuthentication()).thenReturn(authn);
        val chain = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        chain.addPolicy(new AuthenticationDateRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 10, 0));

        assertTrue(chain.shouldParticipateInSso(RegisteredServiceTestUtils.getRegisteredService(), state));
    }

    @Test
    public void verifySsoParticipationByLastUsedTimeFails() {
        val state = mock(TicketState.class);
        when(state.getLastTimeUsed()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(10));
        val chain = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        chain.addPolicy(new LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 1, 0));

        assertFalse(chain.shouldParticipateInSso(RegisteredServiceTestUtils.getRegisteredService(), state));
    }

    @Test
    public void verifySsoParticipationByLastUsedTimePasses() {
        val state = mock(TicketState.class);
        when(state.getLastTimeUsed()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(5));
        val chain = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        chain.addPolicy(new LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 10, 0));

        assertTrue(chain.shouldParticipateInSso(RegisteredServiceTestUtils.getRegisteredService(), state));
    }

    @Test
    public void verifyPolicies() {
        val chain = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        chain.addPolicies(new LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 10, 0));
        assertFalse(chain.getPolicies().isEmpty());
        assertEquals(TriStateBoolean.TRUE, chain.getCreateCookieOnRenewedAuthentication());
    }
}
