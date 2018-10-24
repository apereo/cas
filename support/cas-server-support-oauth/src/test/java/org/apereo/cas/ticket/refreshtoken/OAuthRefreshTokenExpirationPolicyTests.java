package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.ticket.BaseOAuthExpirationPolicyTests;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;

/**
 * This is {@link OAuthRefreshTokenExpirationPolicyTests}.
 *
 * @since 5.3.0
 */
@TestPropertySource(properties = "cas.logout.removeDescendantTickets=true")
public class OAuthRefreshTokenExpirationPolicyTests extends BaseOAuthExpirationPolicyTests {
    @Test
    public void verifyRefreshTokenExpiryWhenTgtIsExpired() {
        val tgt = newTicketGrantingTicket();
        val at = newAccessToken(tgt);
        val rt = newRefreshToken(at);

        assertFalse("Refresh token should not be expired", rt.isExpired());
        tgt.markTicketExpired();
        assertTrue("Refresh token should not be expired when TGT is expired", rt.isExpired());
    }

    @Test
    public void verifySerializeAnOAuthRefreshTokenExpirationPolicyToJson() throws Exception {
        val policyWritten = new OAuthRefreshTokenExpirationPolicy(1234L);
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, OAuthRefreshTokenExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
