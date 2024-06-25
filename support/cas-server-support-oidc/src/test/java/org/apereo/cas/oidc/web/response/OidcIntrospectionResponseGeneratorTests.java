package org.apereo.cas.oidc.web.response;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.response.introspection.OAuth20IntrospectionResponseGenerator;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcIntrospectionResponseGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("OIDC")
class OidcIntrospectionResponseGeneratorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcIntrospectionResponseGenerator")
    private OAuth20IntrospectionResponseGenerator oidcIntrospectionResponseGenerator;

    @Test
    void verifyOperation() throws Throwable {
        val accessToken = getAccessToken(UUID.randomUUID().toString());
        val oidcRegisteredService = getOidcRegisteredService(accessToken.getClientId());
        servicesManager.save(oidcRegisteredService);
        ticketRegistry.addTicket(accessToken);

        assertTrue(oidcIntrospectionResponseGenerator.supports(accessToken));
        assertEquals(Ordered.HIGHEST_PRECEDENCE, oidcIntrospectionResponseGenerator.getOrder());
        val response = oidcIntrospectionResponseGenerator.generate(accessToken.getId(), accessToken);
        assertNotNull(response.getIss());
        assertTrue(response.isActive());
        assertNotNull(response.getScope());
    }

    @Test
    void verifyDPoPOperation() throws Throwable {
        val accessToken = getAccessToken(RegisteredServiceTestUtils.getAuthentication("casuser",
            Map.of(OAuth20Constants.DPOP_CONFIRMATION, List.of(UUID.randomUUID().toString()))), UUID.randomUUID().toString());
        val oidcRegisteredService = getOidcRegisteredService(accessToken.getClientId());
        servicesManager.save(oidcRegisteredService);
        ticketRegistry.addTicket(accessToken);
        val response = oidcIntrospectionResponseGenerator.generate(accessToken.getId(), accessToken);
        assertNotNull(response.getIss());
        assertNotNull(response.getScope());
        assertTrue(response.isActive());
        assertNotNull(response.getConfirmation().getJkt());
    }
}
