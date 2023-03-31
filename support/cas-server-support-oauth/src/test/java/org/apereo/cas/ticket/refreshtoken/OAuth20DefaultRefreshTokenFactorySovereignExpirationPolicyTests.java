package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthRefreshTokenExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DefaultRefreshTokenFactorySovereignExpirationPolicyTests}.
 *
 * @since 7.0.0
 */
@Tag("OAuthToken")
@TestPropertySource(properties = "cas.logout.remove-descendant-tickets=false")
public class OAuth20DefaultRefreshTokenFactorySovereignExpirationPolicyTests extends AbstractOAuth20Tests {
    @Test
    public void verifyOperationWithExpPolicy() {
        val registeredService = getRegisteredService("https://rt.oauth.org", "clientid-rt", "secret-at");
        registeredService.setRefreshTokenExpirationPolicy(
            new DefaultRegisteredServiceOAuthRefreshTokenExpirationPolicy("PT100S"));
        servicesManager.save(registeredService);
        TicketGrantingTicket tgt = new MockTicketGrantingTicket("casuser");
        val token = oAuthRefreshTokenFactory.create(RegisteredServiceTestUtils.getService("https://rt.oauth.org"),
            RegisteredServiceTestUtils.getAuthentication(),
            tgt,
            Set.of("Scope1", "Scope2"), "clientid-rt",
            "at-1234567890", Map.of(), OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        assertNotNull(token);
        assertNotNull(defaultAccessTokenFactory.get(OAuth20RefreshToken.class));

        assertFalse(token.isExpired(), "Refresh token should not be expired");
        tgt.markTicketExpired();
        assertFalse(token.isExpired(),
            "Refresh token must not expire when TGT is expired and removeDescendantTickets is false");
    }
}
