package org.apereo.cas.oidc.web.controllers.jwks;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;

import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcJwksEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
public class OidcJwksEndpointControllerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcJwksController")
    protected OidcJwksEndpointController oidcJwksEndpointController;

    @Test
    public void verifyOperation() {
        val model = new ExtendedModelMap();
        val request = getHttpRequestForEndpoint(OidcConstants.JWKS_URL);
        val response = new MockHttpServletResponse();

        val result = oidcJwksEndpointController.handleRequestInternal(request, response, model);
        assertTrue(result.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void verifyBadEndpointRequest() {
        val model = new ExtendedModelMap();
        val request = getHttpRequestForEndpoint("unknown/issuer");
        request.setRequestURI("unknown/issuer");
        val response = new MockHttpServletResponse();
        val mv = oidcJwksEndpointController.handleRequestInternal(request, response, model);
        assertEquals(HttpStatus.NOT_FOUND, mv.getStatusCode());
    }

    @Test
    public void verifyFails() {
        val model = new ExtendedModelMap();
        val request = getHttpRequestForEndpoint(OidcConstants.JWKS_URL);
        val response = mock(HttpServletResponse.class);
        doThrow(new RuntimeException()).when(response).setContentType(anyString());

        val result = oidcJwksEndpointController.handleRequestInternal(request, response, model);
        assertTrue(result.getStatusCode().is4xxClientError());
    }
}
