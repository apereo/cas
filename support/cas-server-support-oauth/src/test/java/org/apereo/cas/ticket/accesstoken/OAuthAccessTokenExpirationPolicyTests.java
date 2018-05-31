package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.ticket.BaseOAuthExpirationPolicyTests;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@TestPropertySource(properties = "cas.logout.removeDescendantTickets=true")
public class OAuthAccessTokenExpirationPolicyTests extends BaseOAuthExpirationPolicyTests {
    @Test
    public void verifyAccessTokenExpiryWhenTgtIsExpired() {
        final var tgt = newTicketGrantingTicket();
        final var at = newAccessToken(tgt);

        assertFalse("Access token should not be expired", at.isExpired());
        tgt.markTicketExpired();
        assertTrue("Access token should not be expired when TGT is expired", at.isExpired());
    }

    @Test
    public void verifySerializeAnOAuthAccessTokenExpirationPolicyToJson() throws Exception {
        final var policyWritten = new OAuthAccessTokenExpirationPolicy(1234L, 5678L);
        MAPPER.writeValue(JSON_FILE, policyWritten);
        final ExpirationPolicy policyRead = MAPPER.readValue(JSON_FILE, OAuthAccessTokenExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
