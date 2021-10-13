package org.apereo.cas.oidc.web.controllers.token;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcRevocationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
public class OidcRevocationEndpointControllerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcRevocationEndpointController")
    protected OidcRevocationEndpointController oidcRevocationEndpointController;

    @Test
    public void verifyGivenAccessTokenInRegistry() {
        val request = getHttpRequestForEndpoint(OidcConstants.REVOCATION_URL);
        val response = new MockHttpServletResponse();
        val mv = oidcRevocationEndpointController.handleRequest(request, response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get("error"));
    }

    @Test
    public void verifyBadEndpointRequest() {
        val request = getHttpRequestForEndpoint("unknown/issuer");
        request.setRequestURI("unknown/issuer");
        val response = new MockHttpServletResponse();
        val mv = oidcRevocationEndpointController.handleRequest(request, response);
        assertEquals(HttpStatus.NOT_FOUND, mv.getStatus());
    }

}
