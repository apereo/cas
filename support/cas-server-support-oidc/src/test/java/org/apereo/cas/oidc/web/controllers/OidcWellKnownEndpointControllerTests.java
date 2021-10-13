package org.apereo.cas.oidc.web.controllers;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.web.controllers.discovery.OidcWellKnownEndpointController;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcWellKnownEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDC")
public class OidcWellKnownEndpointControllerTests extends AbstractOidcTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier("oidcWellKnownController")
    protected OidcWellKnownEndpointController oidcWellKnownController;

    @Test
    public void verifyOperation() throws Exception {
        var request = getHttpRequestForEndpoint("unknown/" + OidcConstants.WELL_KNOWN_URL);
        request.setRequestURI("unknown/issuer");
        var entity = oidcWellKnownController.getWellKnownDiscoveryConfiguration(request, new MockHttpServletResponse());
        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());

        request = getHttpRequestForEndpoint(OidcConstants.WELL_KNOWN_URL);
        entity = oidcWellKnownController.getWellKnownDiscoveryConfiguration(request, new MockHttpServletResponse());
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        val res1 = MAPPER.writeValueAsString(entity);
        assertNotNull(res1);

        request = getHttpRequestForEndpoint(OidcConstants.WELL_KNOWN_OPENID_CONFIGURATION_URL);
        entity = oidcWellKnownController.getWellKnownOpenIdDiscoveryConfiguration(request, new MockHttpServletResponse());
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        val res2 = MAPPER.writeValueAsString(entity);
        assertNotNull(res2);
    }
}
