package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.ticket.BaseOAuth20ExpirationPolicyTests;
import org.apereo.cas.ticket.TicketGrantingTicketAwareTicket;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@TestPropertySource(properties = "cas.ticket.track-descendant-tickets=false")
@Tag("OAuthToken")
class OAuth20AccessTokenExpirationPolicyTests extends BaseOAuth20ExpirationPolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "oAuthTokenExpirationPolicy.json");

    @Test
    void verifyAccessTokenExpiryWhenTgtIsExpired() throws Throwable {
        val tgt = newTicketGrantingTicket();
        val at = newAccessToken(tgt);
        assertFalse(at.isExpired(), "Access token should not be expired");
        tgt.markTicketExpired();
        assertFalse(at.isExpired(), "Access token should not be expired when TGT is expired");
    }

    @Test
    void verifyAccessTokenExpiredAfterSystemTime() {
        val ticket = mock(TicketGrantingTicketAwareTicket.class);
        when(ticket.getCreationTime()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).minusDays(10));
        val exp = new OAuth20AccessTokenExpirationPolicy(100, 100);
        assertTrue(exp.isExpired(ticket));
    }

    @Test
    void verifyAccessTokenExpiredAfterTimeToKill() {
        val ticket = mock(TicketGrantingTicketAwareTicket.class);
        when(ticket.getCreationTime()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC));
        when(ticket.getLastTimeUsed()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).minusDays(10));
        val exp = new OAuth20AccessTokenExpirationPolicy(100, 100);
        assertTrue(exp.isExpired(ticket));
    }

    @Test
    void verifySerializeAnOAuthAccessTokenExpirationPolicyToJson() throws Throwable {
        val policyWritten = new OAuth20AccessTokenExpirationPolicy(1234L, 5678L);
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, OAuth20AccessTokenExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
