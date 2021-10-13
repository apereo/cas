package org.apereo.cas.oidc.web.controllers.token;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;

import lombok.val;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAccessTokenEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
public class OidcAccessTokenEndpointControllerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcAccessTokenController")
    protected OidcAccessTokenEndpointController oidcAccessTokenEndpointController;

    @Test
    public void verifyBadEndpointRequest() throws Exception {
        val request = getHttpRequestForEndpoint("unknown/issuer");
        request.setRequestURI("unknown/issuer");
        val response = new MockHttpServletResponse();
        val mv = oidcAccessTokenEndpointController.handleRequest(request, response);
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, mv.getStatus());
    }

    @Test
    public void verifyClientNoCode() throws Exception {
        val request = getHttpRequestForEndpoint(OidcConstants.ACCESS_TOKEN_URL);
        val response = new MockHttpServletResponse();
        oidcAccessTokenEndpointController.handleRequest(request, response);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
        oidcAccessTokenEndpointController.handleGetRequest(request, response);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
    }

}
