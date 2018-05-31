package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.ticket.BaseOAuthExpirationPolicyTests;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@TestPropertySource(properties = "cas.logout.removeDescendantTickets=false")
public class OAuthAccessTokenSovereignExpirationPolicyTests extends BaseOAuthExpirationPolicyTests {
    @Test
    public void verifyAccessTokenExpiryWhenTgtIsExpired() {
        final var tgt = newTicketGrantingTicket();
        final var at = newAccessToken(tgt);

        assertFalse("Access token must not be expired", at.isExpired());
        tgt.markTicketExpired();
        assertFalse("Access token must not be expired when TGT is expired", at.isExpired());
    }

}
