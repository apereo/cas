package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.ticket.BaseOAuth20ExpirationPolicyTests;
import org.apereo.cas.ticket.TicketState;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@TestPropertySource(properties = "cas.logout.removeDescendantTickets=true")
@Tag("OAuth")
public class OAuth20AccessTokenExpirationPolicyTests extends BaseOAuth20ExpirationPolicyTests {
    @Test
    public void verifyAccessTokenExpiryWhenTgtIsExpired() {
        val tgt = newTicketGrantingTicket();
        val at = newAccessToken(tgt);
        assertFalse(at.isExpired(), "Access token should not be expired");
        tgt.markTicketExpired();
        assertTrue(at.isExpired(), "Access token should not be expired when TGT is expired");
    }

    @Test
    public void verifyAccessTokenExpiredAfterSystemTime() {
        val ticket = mock(TicketState.class);
        when(ticket.getCreationTime()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).minusDays(10));
        val exp = new OAuth20AccessTokenExpirationPolicy(100, 100);
        assertTrue(exp.isExpired(ticket));
    }

    @Test
    public void verifyAccessTokenExpiredAfterTimeToKill() {
        val ticket = mock(TicketState.class);
        when(ticket.getCreationTime()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC));
        when(ticket.getLastTimeUsed()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).minusDays(10));
        val exp = new OAuth20AccessTokenExpirationPolicy(100, 100);
        assertTrue(exp.isExpired(ticket));
    }

    @Test
    public void verifySerializeAnOAuthAccessTokenExpirationPolicyToJson() throws Exception {
        val policyWritten = new OAuth20AccessTokenExpirationPolicy(1234L, 5678L);
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, OAuth20AccessTokenExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
