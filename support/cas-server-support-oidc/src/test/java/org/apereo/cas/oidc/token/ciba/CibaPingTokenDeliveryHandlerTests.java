package org.apereo.cas.oidc.token.ciba;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcCibaRequest;
import org.apereo.cas.oidc.ticket.OidcCibaRequestFactory;
import org.apereo.cas.oidc.web.controllers.ciba.CibaRequestContext;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.util.MockWebServer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CibaPingTokenDeliveryHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OIDC")
@TestPropertySource(properties = "cas.http-client.host-name-verifier=none")
public class CibaPingTokenDeliveryHandlerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcCibaPingTokenDeliveryHandler")
    private CibaTokenDeliveryHandler oidcCibaPushTokenDeliveryHandler;

    @Test
    void verifyOperation() throws Throwable {
        assertEquals(OidcBackchannelTokenDeliveryModes.PING, oidcCibaPushTokenDeliveryHandler.getDeliveryMode());
        try (val webServer = new MockWebServer(true)) {
            webServer.start();
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            registeredService.setBackchannelTokenDeliveryMode(OidcBackchannelTokenDeliveryModes.PING.getMode());
            registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
            registeredService.setBackchannelClientNotificationEndpoint("https://localhost:%s".formatted(webServer.getPort()));
            servicesManager.save(registeredService);
            
            assertTrue(oidcCibaPushTokenDeliveryHandler.supports(registeredService));
            
            val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString());
            val cibaRequest = newCibaRequest(registeredService, principal);
            val delivery = oidcCibaPushTokenDeliveryHandler.deliver(registeredService, cibaRequest);
            assertTrue(delivery.containsKey(OidcConstants.AUTH_REQ_ID));
            val ticket = ticketRegistry.getTicket(cibaRequest.getId(), OidcCibaRequest.class);
            assertNotNull(ticket);
            assertTrue(ticket.isReady());
        }
    }

    private OidcCibaRequest newCibaRequest(final OidcRegisteredService registeredService,
                                           final Principal principal) throws Throwable {
        val cibaRequestContext = CibaRequestContext.builder()
            .clientNotificationToken(UUID.randomUUID().toString())
            .clientId(registeredService.getClientId())
            .scope(Set.of(OidcConstants.StandardScopes.OPENID.getScope()))
            .userCode(UUID.randomUUID().toString())
            .principal(principal)
            .build();
        val cibaFactory = (OidcCibaRequestFactory) defaultTicketFactory.get(OidcCibaRequest.class);
        val cibaRequestId = cibaFactory.create(cibaRequestContext);
        ticketRegistry.addTicket(cibaRequestId);
        return cibaRequestId;
    }

}
