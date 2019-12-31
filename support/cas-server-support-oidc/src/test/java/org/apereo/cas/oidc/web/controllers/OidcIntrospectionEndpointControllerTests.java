package org.apereo.cas.oidc.web.controllers;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.web.controllers.introspection.OidcIntrospectionEndpointController;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.EncodingUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.HttpConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcIntrospectionEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
public class OidcIntrospectionEndpointControllerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcIntrospectionEndpointController")
    protected OidcIntrospectionEndpointController oidcIntrospectionEndpointController;

    @Test
    public void verifyOperationWithValidTicket() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val auth = "clientid:secret";
        val value = EncodingUtils.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        request.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX + value);

        val accessToken = getAccessToken();
        this.ticketRegistry.addTicket(accessToken);
        request.addParameter(OAuth20Constants.TOKEN, accessToken.getId());
        val result = oidcIntrospectionEndpointController.handleRequest(request, response);
        assertNotNull(result.getBody());
        assertTrue(Instant.ofEpochSecond(result.getBody().getExp()).isAfter(Instant.ofEpochSecond(result.getBody().getIat())));
        assertTrue(result.getBody().isActive());
        assertEquals(accessToken.getScopes(), Set.of(result.getBody().getScope().split(" ")));
    }

    @Test
    public void verifyOperationWithInvalidTicket() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val auth = "clientid:secret";
        val value = EncodingUtils.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        request.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX + value);

        val accessToken = getAccessToken();
        request.addParameter(OAuth20Constants.TOKEN, accessToken.getId());
        val result = oidcIntrospectionEndpointController.handleRequest(request, response);
        assertNotNull(result.getBody());
        assertFalse(result.getBody().isActive());
    }
}
