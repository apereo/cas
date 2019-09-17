package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.ticket.BaseOAuth20ExpirationPolicyTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuthRefreshTokenExpirationPolicyTests}.
 *
 * @since 5.3.0
 */
@TestPropertySource(properties = "cas.logout.removeDescendantTickets=true")
@Tag("OAuth")
public class OAuthRefreshTokenExpirationPolicyTests extends BaseOAuth20ExpirationPolicyTests {
    @Test
    public void verifyRefreshTokenExpiryWhenTgtIsExpired() {
        val tgt = newTicketGrantingTicket();
        val at = newAccessToken(tgt);
        val rt = newRefreshToken(at);

        assertFalse(rt.isExpired(), "Refresh token should not be expired");
        tgt.markTicketExpired();
        assertTrue(rt.isExpired(), "Refresh token should not be expired when TGT is expired");
    }

    @Test
    public void verifySerializeAnOAuthRefreshTokenExpirationPolicyToJson() throws Exception {
        val policyWritten = new OAuthRefreshTokenExpirationPolicy(1234L);
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, OAuthRefreshTokenExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
