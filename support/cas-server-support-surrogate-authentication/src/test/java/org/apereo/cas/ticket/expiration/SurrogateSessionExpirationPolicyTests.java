package org.apereo.cas.ticket.expiration;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.mock.MockTicketGrantingTicket;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogateSessionExpirationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("ExpirationPolicy")
public class SurrogateSessionExpirationPolicyTests {

    @Test
    public void verifyDefault() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val policy = new SurrogateSessionExpirationPolicy();
        assertEquals(BaseDelegatingExpirationPolicy.POLICY_NAME_DEFAULT, policy.getExpirationPolicyNameFor(tgt));
    }

    @Test
    public void verifySurrogate() {
        val tgt = new MockTicketGrantingTicket("casuser", Map.of(),
            Map.of(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL, List.of("principal"),
                SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER, List.of("user")));
        val policy = new SurrogateSessionExpirationPolicy();
        assertEquals(SurrogateSessionExpirationPolicy.POLICY_NAME_SURROGATE, policy.getExpirationPolicyNameFor(tgt));
    }
}
