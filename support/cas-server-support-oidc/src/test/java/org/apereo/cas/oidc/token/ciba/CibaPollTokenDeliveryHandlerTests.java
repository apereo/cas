package org.apereo.cas.oidc.token.ciba;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcCibaRequest;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CibaPollTokenDeliveryHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OIDC")
class CibaPollTokenDeliveryHandlerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcCibaPollTokenDeliveryHandler")
    private CibaTokenDeliveryHandler oidcCibaPollTokenDeliveryHandler;

    @Test
    void verifyOperation() throws Throwable {
        assertEquals(OidcBackchannelTokenDeliveryModes.POLL, oidcCibaPollTokenDeliveryHandler.getDeliveryMode());
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setBackchannelTokenDeliveryMode(OidcBackchannelTokenDeliveryModes.POLL.getMode());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        servicesManager.save(registeredService);
        assertTrue(oidcCibaPollTokenDeliveryHandler.supports(registeredService));

        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString());
        val cibaRequest = newCibaRequest(registeredService, principal);
        val delivery = oidcCibaPollTokenDeliveryHandler.deliver(registeredService, cibaRequest);
        assertTrue(delivery.containsKey(OidcConstants.AUTH_REQ_ID));
        val ticket = ticketRegistry.getTicket(cibaRequest.getId(), OidcCibaRequest.class);
        assertNotNull(ticket);
        assertTrue(ticket.isReady());
    }


}
