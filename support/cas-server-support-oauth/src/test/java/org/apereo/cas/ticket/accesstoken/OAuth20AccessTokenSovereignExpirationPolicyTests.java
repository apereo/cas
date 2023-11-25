package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.ticket.BaseOAuth20ExpirationPolicyTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@TestPropertySource(properties = "cas.ticket.track-descendant-tickets=false")
@Tag("OAuthToken")
class OAuth20AccessTokenSovereignExpirationPolicyTests extends BaseOAuth20ExpirationPolicyTests {
    @Test
    void verifyAccessTokenExpiryWhenTgtIsExpired() throws Throwable {
        val tgt = newTicketGrantingTicket();
        val at = newAccessToken(tgt);

        assertFalse(at.isExpired(), "Access token must not be expired");
        tgt.markTicketExpired();
        assertFalse(at.isExpired(), "Access token must not be expired when TGT is expired");
    }

}
