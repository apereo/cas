package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.AbstractOidcTrustAnchorFederationTests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import com.nimbusds.jwt.SignedJWT;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcTrustAnchorFetchEndpointControllerTests}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@Tag("OIDCWeb")
class OidcTrustAnchorFetchEndpointControllerTests extends AbstractOidcTrustAnchorFederationTests {

    private static final String OP = "http://op";
    private static final String RP = "http://rp";

    private static final String FETCH_ENDPOINT_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.FETCH_FEDERATION_URL;

    @Test
    void verifyInvalidIssuer() throws Exception {
        mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=" + OP)
                        .with(request -> {
                            request.setScheme("https");
                            request.setServerName("unknown.example.org");
                            request.setContextPath("/cas");
                            request.setServletPath("/cas");
                            request.setServerPort(443);
                            return request;
                        }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST))
                .andExpect(jsonPath("$.error_description").value("Invalid issuer"));
    }

    @Test
    void verifyMissingEntity() throws Exception {
        mockMvc.perform(get(FETCH_ENDPOINT_URL)
                        .with(withHttpRequestProcessor()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST))
                .andExpect(jsonPath("$.error_description").value("Invalid entity"));
    }

    @Test
    void verifyInvalidEntity() throws Exception {
        mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=http://fake")
                        .with(withHttpRequestProcessor()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST))
                .andExpect(jsonPath("$.error_description").value("Invalid entity"));
    }

    @Test
    void verifyRelyingParty() throws Exception {
        val result = mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=" + RP)
                        .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(OidcConstants.ENTITY_STATEMENT_CONTENT_TYPE))
                .andReturn();
        val jwt = SignedJWT.parse(result.getResponse().getContentAsString());
        val claims = jwt.getJWTClaimsSet();
        assertEquals("https://sso.example.org/cas/oidc", claims.getIssuer());
        assertEquals(RP, claims.getSubject());
        assertNull(claims.getClaim("authority_hints"));
        assertNotNull(claims.getClaim("jwks"));
        val metadata = (Map) claims.getClaim("metadata");
        assertNotNull(metadata.get("federation_entity"));
        assertNull(metadata.get("openid_provider"));
        assertNotNull(metadata.get("openid_relying_party"));
    }

    @Test
    void verifyOpenidProvider() throws Exception {
        val result = mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=" + OP)
                        .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(OidcConstants.ENTITY_STATEMENT_CONTENT_TYPE))
                .andReturn();
        val jwt = SignedJWT.parse(result.getResponse().getContentAsString());
        val claims = jwt.getJWTClaimsSet();
        assertEquals("https://sso.example.org/cas/oidc", claims.getIssuer());
        assertEquals(OP, claims.getSubject());
        assertNull(claims.getClaim("authority_hints"));
        assertNotNull(claims.getClaim("jwks"));
        val metadata = (Map) claims.getClaim("metadata");
        assertNotNull(metadata.get("federation_entity"));
        assertNotNull(metadata.get("openid_provider"));
        assertNull(metadata.get("openid_relying_party"));
    }
}
