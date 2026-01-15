package org.apereo.cas.ticket.refreshtoken;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthRefreshTokenExpirationPolicy;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.Ticket;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
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
        void verifyMaxActiveTokensAllowed() throws Throwable {
            val registeredService = getRegisteredService(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString());
            registeredService.setRefreshTokenExpirationPolicy(new DefaultRegisteredServiceOAuthRefreshTokenExpirationPolicy("PT10S", 3));
            servicesManager.save(registeredService);

            val tgt = new MockTicketGrantingTicket("casuser");
            ticketRegistry.addTicket(tgt);
            for (var i = 0; i < 3; i++) {
                val token = createRefreshToken(tgt, registeredService);
                assertNotNull(token);
                ticketRegistry.addTicket(token);
            }
            ticketRegistry.updateTicket(tgt);
            assertThrows(IllegalArgumentException.class, () -> createRefreshToken(tgt, registeredService));
        }

        @Test
        void verifyOperationWithExpPolicy() throws Throwable {
            val registeredService = getRegisteredService("https://rt.oauth.org", "clientid-rt", "secret-at");
            registeredService.setRefreshTokenExpirationPolicy(
                new DefaultRegisteredServiceOAuthRefreshTokenExpirationPolicy("PT100S", 0));
            servicesManager.save(registeredService);
            val ticketGrantingTicket = new MockTicketGrantingTicket("casuser");
            val token = createRefreshToken(ticketGrantingTicket, registeredService);
            assertNotNull(token);
            assertNotNull(defaultAccessTokenFactory.get(OAuth20RefreshToken.class));
            assertInstanceOf(OAuth20RefreshTokenExpirationPolicy.class, token.getExpirationPolicy());
        }

        private OAuth20RefreshToken createRefreshToken(final Ticket ticketGrantingTicket, final OAuthRegisteredService registeredService) throws Throwable {
            return defaultRefreshTokenFactory.create(
                RegisteredServiceTestUtils.getService(registeredService.getServiceId()),
                RegisteredServiceTestUtils.getAuthentication(),
                ticketGrantingTicket,
                Set.of("Scope1", "Scope2"), registeredService.getClientId(),
                "AT-1234567890", Map.of(),
                OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.ticket.track-descendant-tickets=false")
    class TrackingDescendantTicketsDisabled extends AbstractOAuth20Tests {

        @Test
        void verifyOperationWithExpPolicy() throws Throwable {
            val registeredService = getRegisteredService("https://rt.oauth.org", "clientid-rt", "secret-at");
            registeredService.setRefreshTokenExpirationPolicy(
                new DefaultRegisteredServiceOAuthRefreshTokenExpirationPolicy("PT100S", 0));
            servicesManager.save(registeredService);
            val token = defaultRefreshTokenFactory.create(RegisteredServiceTestUtils.getService("https://rt.oauth.org"),
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
