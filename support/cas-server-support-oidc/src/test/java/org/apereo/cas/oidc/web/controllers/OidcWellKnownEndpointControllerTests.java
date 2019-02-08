package org.apereo.cas.oidc.web.controllers;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.web.controllers.discovery.OidcWellKnownEndpointController;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcWellKnownEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class OidcWellKnownEndpointControllerTests extends AbstractOidcTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    @Qualifier("oidcWellKnownController")
    protected OidcWellKnownEndpointController oidcWellKnownController;

    @Test
    public void verifyOperation() throws Exception {
        val res1 = MAPPER.writer().writeValueAsString(oidcWellKnownController.getWellKnownDiscoveryConfiguration());
        assertNotNull(res1);
        val res2 = MAPPER.writer().writeValueAsString(oidcWellKnownController.getWellKnownOpenIdDiscoveryConfiguration());
        assertNotNull(res2);
    }
}
