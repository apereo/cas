package org.apereo.cas.oidc.token.ciba;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
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
 * This is {@link CibaPushTokenDeliveryHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OIDC")
@TestPropertySource(properties = "cas.http-client.host-name-verifier=none")
class CibaPushTokenDeliveryHandlerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcCibaPushTokenDeliveryHandler")
    private CibaTokenDeliveryHandler oidcCibaPushTokenDeliveryHandler;

    @Test
    void verifyOperation() throws Throwable {
        assertEquals(OidcBackchannelTokenDeliveryModes.PUSH, oidcCibaPushTokenDeliveryHandler.getDeliveryMode());
        try (val webServer = new MockWebServer(true)) {
            webServer.start();
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            registeredService.setBackchannelTokenDeliveryMode(OidcBackchannelTokenDeliveryModes.PUSH.getMode());
            registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
            registeredService.setBackchannelClientNotificationEndpoint("https://localhost:%s".formatted(webServer.getPort()));
            servicesManager.save(registeredService);
            
            assertTrue(oidcCibaPushTokenDeliveryHandler.supports(registeredService));
            
            val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString());
            val delivery = oidcCibaPushTokenDeliveryHandler.deliver(registeredService, newCibaRequest(registeredService, principal));
            assertTrue(delivery.containsKey(OAuth20Constants.ACCESS_TOKEN));
            assertTrue(delivery.containsKey(OAuth20Constants.EXPIRES_IN));
            assertTrue(delivery.containsKey(OAuth20Constants.TOKEN_TYPE));
            assertTrue(delivery.containsKey(OidcConstants.ID_TOKEN));
            assertTrue(delivery.containsKey(OidcConstants.AUTH_REQ_ID));
        }
    }


}
