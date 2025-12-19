package org.apereo.cas.oidc.token.ciba;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcCibaRequest;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.util.MockWebServer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CibaPingTokenDeliveryHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OIDC")
@TestPropertySource(properties = "cas.http-client.host-name-verifier=none")
class CibaPingTokenDeliveryHandlerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcCibaPingTokenDeliveryHandler")
    private CibaTokenDeliveryHandler oidcCibaPingTokenDeliveryHandler;

    @Test
    void verifyOperation() throws Throwable {
        assertEquals(OidcBackchannelTokenDeliveryModes.PING, oidcCibaPingTokenDeliveryHandler.getDeliveryMode());
        try (val webServer = new MockWebServer(true)) {
            webServer.start();
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            registeredService.setBackchannelTokenDeliveryMode(OidcBackchannelTokenDeliveryModes.PING.getMode());
            registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
            registeredService.setBackchannelClientNotificationEndpoint("https://localhost:%s".formatted(webServer.getPort()));
            servicesManager.save(registeredService);
            
            assertTrue(oidcCibaPingTokenDeliveryHandler.supports(registeredService));
            
            val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString());
            val cibaRequest = newCibaRequest(registeredService, principal);
            val delivery = oidcCibaPingTokenDeliveryHandler.deliver(registeredService, cibaRequest);
            assertTrue(delivery.containsKey(OidcConstants.AUTH_REQ_ID));
            val ticket = ticketRegistry.getTicket(cibaRequest.getId(), OidcCibaRequest.class);
            assertNotNull(ticket);
            assertTrue(ticket.isReady());
        }
    }


}
