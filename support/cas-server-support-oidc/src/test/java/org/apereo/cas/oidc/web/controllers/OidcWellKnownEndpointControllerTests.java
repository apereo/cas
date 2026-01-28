package org.apereo.cas.oidc.web.controllers;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcWellKnownEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDCWeb")
class OidcWellKnownEndpointControllerTests extends AbstractOidcTests {

    @Test
    void verifyUnknownIssuer() throws Exception {
        mockMvc.perform(get("/cas/oidc/" + OidcConstants.WELL_KNOWN_URL)
                .with(withHttpRequestProcessor())
                .with(r -> {
                    r.setServerName("sso2.example.org");
                    return r;
                }))
            .andExpect(status().isNotFound());
    }

    @Test
    void verifyWellKnownDiscoveryConfiguration() throws Exception {
        mockMvc.perform(get("/cas/oidc/" + OidcConstants.WELL_KNOWN_URL)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.issuer").exists());
    }

    @Test
    void verifyWellKnownOpenIdConfiguration() throws Exception {
        mockMvc.perform(get("/cas/oidc/" + OidcConstants.WELL_KNOWN_OPENID_CONFIGURATION_URL)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.issuer").exists());
    }

    @Test
    void verifyWebFingerEndpoint() throws Exception {
        mockMvc.perform(get("/cas/oidc/" + OidcConstants.WELL_KNOWN_URL + "/webfinger")
                .queryParam("resource", "acct:casuser@sso.example.org")
                .queryParam("rel", OidcConstants.WEBFINGER_REL)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isOk());
    }

    @Test
    void verifyWebFingerEndpointWithoutRel() throws Exception {
        mockMvc.perform(get("/cas/oidc/" + OidcConstants.WELL_KNOWN_URL + "/webfinger")
                .queryParam("resource", "acct:casuser@sso.example.org")
                .with(withHttpRequestProcessor()))
            .andExpect(status().isOk());
    }

    @Test
    void verifyWebFingerEndpointMissingResource() throws Exception {
        mockMvc.perform(get("/cas/oidc/" + OidcConstants.WELL_KNOWN_URL + "/webfinger")
                .with(withHttpRequestProcessor()))
            .andExpect(status().isBadRequest());
    }
}
