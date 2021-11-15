package org.apereo.cas.ticket.query;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketState;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlAttributeQueryTicketExpirationPolicyBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("ExpirationPolicy")
public class SamlAttributeQueryTicketExpirationPolicyBuilderTests {
    @Test
    public void verifyOperation() {
        val properties = new CasConfigurationProperties();
        properties.getAuthn().getSamlIdp().getTicket().getAttributeQuery().setTimeToKillInSeconds(5);
        val builder = new SamlAttributeQueryTicketExpirationPolicyBuilder(properties);
        val ticket = mock(TicketState.class);
        when(ticket.getCreationTime()).thenReturn(ZonedDateTime.now(Clock.systemUTC()).plusSeconds(2));
        assertFalse(builder.toTicketExpirationPolicy().isExpired(ticket));
        when(ticket.getCreationTime()).thenReturn(ZonedDateTime.now(Clock.systemUTC()).minusSeconds(10));
        assertTrue(builder.toTicketExpirationPolicy().isExpired(ticket));
    }

    @Test
    public void verifyNeverExpiresOperation() {
        val properties = new CasConfigurationProperties();
        properties.getAuthn().getSamlIdp().getTicket().getAttributeQuery().setTimeToKillInSeconds(0);
        val builder = new SamlAttributeQueryTicketExpirationPolicyBuilder(properties);
        val ticket = mock(TicketState.class);
        when(ticket.getCreationTime()).thenReturn(ZonedDateTime.now(Clock.systemUTC()).minusDays(2));
        assertFalse(builder.toTicketExpirationPolicy().isExpired(ticket));
    }
}
