package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthRefreshTokenExpirationPolicy;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DefaultRefreshTokenFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OAuthToken")
class OAuth20DefaultRefreshTokenFactoryTests extends AbstractOAuth20Tests {

    @Nested
    @TestPropertySource(properties = "cas.ticket.track-descendant-tickets=true")
    class TrackingDescendantTicketsEnabled extends AbstractOAuth20Tests {

        @Test
        void verifyOperationWithExpPolicy() throws Throwable {
            val registeredService = getRegisteredService("https://rt.oauth.org", "clientid-rt", "secret-at");
            registeredService.setRefreshTokenExpirationPolicy(
                new DefaultRegisteredServiceOAuthRefreshTokenExpirationPolicy("PT100S"));
            servicesManager.save(registeredService);
            val token = oAuthRefreshTokenFactory.create(RegisteredServiceTestUtils.getService("https://rt.oauth.org"),
                RegisteredServiceTestUtils.getAuthentication(),
                new MockTicketGrantingTicket("casuser"),
                Set.of("Scope1", "Scope2"), "clientid-rt",
                "at-1234567890", Map.of(), OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
            assertNotNull(token);
            assertNotNull(defaultAccessTokenFactory.get(OAuth20RefreshToken.class));
            assertInstanceOf(OAuth20RefreshTokenExpirationPolicy.class, token.getExpirationPolicy());
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.ticket.track-descendant-tickets=false")
    class TrackingDescendantTicketsDisabled extends AbstractOAuth20Tests {

        @Test
        void verifyOperationWithExpPolicy() throws Throwable {
            val registeredService = getRegisteredService("https://rt.oauth.org", "clientid-rt", "secret-at");
            registeredService.setRefreshTokenExpirationPolicy(
                new DefaultRegisteredServiceOAuthRefreshTokenExpirationPolicy("PT100S"));
            servicesManager.save(registeredService);
            val token = oAuthRefreshTokenFactory.create(RegisteredServiceTestUtils.getService("https://rt.oauth.org"),
                RegisteredServiceTestUtils.getAuthentication(),
                new MockTicketGrantingTicket("casuser"),
                Set.of("Scope1", "Scope2"), "clientid-rt",
                "at-1234567890", Map.of(), OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
            assertNotNull(token);
            assertNotNull(defaultAccessTokenFactory.get(OAuth20RefreshToken.class));
            assertInstanceOf(OAuth20RefreshTokenStandaloneExpirationPolicy.class, token.getExpirationPolicy());
        }
    }
}
