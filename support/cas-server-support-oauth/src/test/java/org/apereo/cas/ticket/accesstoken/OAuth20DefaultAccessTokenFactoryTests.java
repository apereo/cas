package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DefaultAccessTokenFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OAuthToken")
@TestPropertySource(properties = "cas.ticket.track-descendant-tickets=true")
class OAuth20DefaultAccessTokenFactoryTests extends AbstractOAuth20Tests {

    @Test
    void verifyOperationCreate() throws Throwable {
        val registeredService = getRegisteredService("https://app.oauth.org", "clientid-at", "secret-at");
        registeredService.setAccessTokenExpirationPolicy(new DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy("PT10S", "PT10S", 0));
        servicesManager.save(registeredService);
        val token = createAccessToken(new MockTicketGrantingTicket("casuser"), registeredService);
        assertNotNull(token);
        assertNotNull(defaultAccessTokenFactory.get(OAuth20AccessToken.class));
    }

    @Test
    void verifyMaxActiveTokensAllowed() throws Throwable {
        val registeredService = getRegisteredService(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString());
        registeredService.setAccessTokenExpirationPolicy(new DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy("PT10S", "PT10S", 3));
        servicesManager.save(registeredService);

        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        for (var i = 0; i < 3; i++) {
            val token = createAccessToken(tgt, registeredService);
            assertNotNull(token);
            ticketRegistry.addTicket(token);
        }
        ticketRegistry.updateTicket(tgt);
        assertThrows(IllegalArgumentException.class, () -> createAccessToken(tgt, registeredService));
    }

    private OAuth20AccessToken createAccessToken(
        final TicketGrantingTicket ticketGrantingTicket,
        final OAuthRegisteredService registeredService) throws Throwable {
        return defaultAccessTokenFactory.create(
            RegisteredServiceTestUtils.getService(registeredService.getServiceId()),
            RegisteredServiceTestUtils.getAuthentication(),
            ticketGrantingTicket,
            Set.of("Scope1", "Scope2"), null, registeredService.getClientId(), Map.of(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
    }
}
