package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
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
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val clientId = UUID.randomUUID().toString();
        servicesManager.save(getOidcRegisteredService(clientId));
        assertEquals(HttpStatus.SC_OK,
            controller.handleRequestInternal(clientId, request, response).getStatusCodeValue());
    }

    @Test
    public void verifyBadRequest() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val clientId = UUID.randomUUID().toString();
        assertEquals(HttpStatus.SC_BAD_REQUEST,
            controller.handleRequestInternal(clientId, request, response).getStatusCodeValue());
    }
}
