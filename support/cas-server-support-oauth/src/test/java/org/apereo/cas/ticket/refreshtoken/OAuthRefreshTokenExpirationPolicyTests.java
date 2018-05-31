package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.ticket.BaseOAuthExpirationPolicyTests;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.junit.Test;
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
        final var tgt = newTicketGrantingTicket();
        final var at = newAccessToken(tgt);
        final var rt = newRefreshToken(at);

        assertFalse("Refresh token should not be expired", rt.isExpired());
        tgt.markTicketExpired();
        assertTrue("Refresh token should not be expired when TGT is expired", rt.isExpired());
    }

    @Test
    public void verifySerializeAnOAuthRefreshTokenExpirationPolicyToJson() throws Exception {
        final var policyWritten = new OAuthRefreshTokenExpirationPolicy(1234L);
        MAPPER.writeValue(JSON_FILE, policyWritten);
        final ExpirationPolicy policyRead = MAPPER.readValue(JSON_FILE, OAuthRefreshTokenExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
