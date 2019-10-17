package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.ticket.BaseOAuth20ExpirationPolicyTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20RefreshTokenExpirationPolicyTests}.
 *
 * @since 5.3.0
 */
@TestPropertySource(properties = "cas.logout.removeDescendantTickets=true")
@Tag("OAuth")
public class OAuth20RefreshTokenExpirationPolicyTests extends BaseOAuth20ExpirationPolicyTests {
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
        val policyWritten = new OAuth20RefreshTokenExpirationPolicy(1234L);
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, OAuth20RefreshTokenExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
