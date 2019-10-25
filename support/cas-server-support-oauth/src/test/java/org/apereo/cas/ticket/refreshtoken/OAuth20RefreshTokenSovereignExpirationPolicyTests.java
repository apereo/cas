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
@Tag("OAuth")
@TestPropertySource(properties = "cas.logout.removeDescendantTickets=false")
public class OAuth20RefreshTokenSovereignExpirationPolicyTests extends BaseOAuth20ExpirationPolicyTests {
    @Test
    public void verifyRefreshTokenExpiryWhenTgtIsExpired() {
        val tgt = newTicketGrantingTicket();
        val at = newAccessToken(tgt);
        val rt = newRefreshToken(at);
        assertFalse(rt.isExpired(), "Refresh token should not be expired");
        tgt.markTicketExpired();
        assertFalse(rt.isExpired(), "Refresh token must not expired when TGT is expired");
    }

}
