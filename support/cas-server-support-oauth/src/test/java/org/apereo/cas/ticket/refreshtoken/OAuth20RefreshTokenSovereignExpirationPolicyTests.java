package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.ticket.BaseOAuth20ExpirationPolicyTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20RefreshTokenSovereignExpirationPolicyTests}.
 *
 * @since 5.3.0
 */
@Tag("OAuthToken")
@TestPropertySource(properties = "cas.ticket.track-descendant-tickets=false")
class OAuth20RefreshTokenSovereignExpirationPolicyTests extends BaseOAuth20ExpirationPolicyTests {
    @Test
    void verifyRefreshTokenExpiryWhenTgtIsExpired() throws Throwable {
        val tgt = newTicketGrantingTicket();
        val at = newAccessToken(tgt);
        val rt = newRefreshToken(at);
        assertFalse(rt.isExpired(), "Refresh token should not be expired");
        tgt.markTicketExpired();
        assertFalse(rt.isExpired(), "Refresh token must not expired when TGT is expired");
    }

}
