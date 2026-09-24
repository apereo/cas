package org.apereo.cas.oidc.web.controllers;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.*;
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

    /**
     * RFC 8414 requires token_endpoint_auth_signing_alg_values_supported to be published whenever
     * private_key_jwt or client_secret_jwt is advertised, and forbids the value none as a signing
     * algorithm. Clients that enforce this refuse to use the authorization server at all when it
     * is missing. Separately, none is advertised as an authentication *method*, which is how a
     * client with no credentials -- a wallet redeeming a pre-authorized code, say -- learns that
     * the token endpoint will accept it.
     */
    @Test
    void verifyTokenEndpointAuthSigningAlgValues() throws Exception {
        mockMvc.perform(get("/cas/oidc/" + OidcConstants.WELL_KNOWN_OPENID_CONFIGURATION_URL)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token_endpoint_auth_methods_supported").value(hasItem("private_key_jwt")))
            .andExpect(jsonPath("$.token_endpoint_auth_methods_supported").value(hasItem("none")))
            .andExpect(jsonPath("$.token_endpoint_auth_signing_alg_values_supported").value(hasItem("RS256")))
            .andExpect(jsonPath("$.token_endpoint_auth_signing_alg_values_supported").value(not(hasItem("none"))));
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
