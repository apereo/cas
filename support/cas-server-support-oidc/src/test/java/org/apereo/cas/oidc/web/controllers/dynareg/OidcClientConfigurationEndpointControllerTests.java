package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcClientConfigurationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDC")
public class OidcClientConfigurationEndpointControllerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcClientConfigurationEndpointController")
    protected OidcClientConfigurationEndpointController controller;

    @Test
    public void verifyBadEndpointRequest() {
        val request = getHttpRequestForEndpoint("unknown/issuer");
        request.setRequestURI("unknown/issuer");
        val response = new MockHttpServletResponse();
        val mv = controller.handleRequestInternal(StringUtils.EMPTY, request, response);
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, mv.getStatusCode());
    }

    @Test
    public void verifyOperation() {
        val request = getHttpRequestForEndpoint(OidcConstants.CLIENT_CONFIGURATION_URL);
        val response = new MockHttpServletResponse();
        val clientId = UUID.randomUUID().toString();
        servicesManager.save(getOidcRegisteredService(clientId));
        assertEquals(HttpStatus.SC_OK,
            controller.handleRequestInternal(clientId, request, response).getStatusCodeValue());
    }

    @Test
    public void verifyBadRequest() {
        val request = getHttpRequestForEndpoint(OidcConstants.CLIENT_CONFIGURATION_URL);
        val response = new MockHttpServletResponse();
        val clientId = UUID.randomUUID().toString();
        assertEquals(HttpStatus.SC_BAD_REQUEST,
            controller.handleRequestInternal(clientId, request, response).getStatusCodeValue());
    }
}
