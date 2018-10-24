package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.ticket.BaseOAuthExpirationPolicyTests;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;

/**
 * This is {@link OAuthRefreshTokenSovereignExpirationPolicyTests}.
 *
 * @since 5.3.0
 */
@TestPropertySource(properties = "cas.logout.removeDescendantTickets=false")
public class OAuthRefreshTokenSovereignExpirationPolicyTests extends BaseOAuthExpirationPolicyTests {
    @Test
    public void verifyRefreshTokenExpiryWhenTgtIsExpired() {
        val tgt = newTicketGrantingTicket();
        val at = newAccessToken(tgt);
        val rt = newRefreshToken(at);
        assertFalse("Refresh token should not be expired", rt.isExpired());
        tgt.markTicketExpired();
        assertFalse("Refresh token must not expired when TGT is expired", rt.isExpired());
    }

}
