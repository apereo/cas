package org.apereo.cas.support.oauth.web.mgmt;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20TokenManagementEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "management.endpoint.oauthTokens.enabled=true",
    "management.endpoints.web.exposure.include=*"
})
@Tag("OAuth")
public class OAuth20TokenManagementEndpointTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oAuth20TokenManagementEndpoint")
    private OAuth20TokenManagementEndpoint tokenManagementEndpoint;

    @Test
    public void verifyOperationWithJwt() {
        val registeredService = getRegisteredService("example1", "secret", new LinkedHashSet<>());
        registeredService.setJwtAccessToken(true);
        servicesManager.save(registeredService);

        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService);
        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        val token = tokenManagementEndpoint.getToken(at);
        assertNotNull(token);
    }

    @Test
    public void verifyOperation() {
        val registeredService = getRegisteredService("example2", "secret", new LinkedHashSet<>());
        servicesManager.save(registeredService);

        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService);
        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        val token = tokenManagementEndpoint.getToken(at);
        assertNotNull(token);
    }
}
