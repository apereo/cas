package org.apereo.cas.oidc.federation;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import com.nimbusds.jwt.SignedJWT;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcWellKnownFederationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("OIDCWeb")
class OidcWellKnownFederationEndpointControllerTests extends AbstractOidcTrustAnchorFederationTests {

    private static final String FEDERATION_ENDPOINT_URL =
        "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.WELL_KNOWN_OPENID_FEDERATION_URL;

    @Nested
    class TrustAnchorFederationEndpointTests extends AbstractOidcTrustAnchorFederationTests {

        @Test
        void verifyTaInvalidIssuer() throws Exception {
            mockMvc.perform(get(FEDERATION_ENDPOINT_URL)
                            .with(request -> {
                                request.setScheme("https");
                                request.setServerName("unknown.example.org");
                                request.setContextPath("/cas");
                                request.setServletPath("/cas");
                                request.setServerPort(443);
                                return request;
                            }))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST));
        }

        @Test
        void verifyTaOperation() throws Exception {
            val result = mockMvc.perform(get(FEDERATION_ENDPOINT_URL)
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(OidcWellKnownFederationEndpointController.ENTITY_STATEMENT_TYPE))
                    .andReturn();
            val jwt = SignedJWT.parse(result.getResponse().getContentAsString());
            val claims = jwt.getJWTClaimsSet();
            assertEquals("https://sso.example.org/cas/oidc", claims.getIssuer());
            assertEquals("https://sso.example.org/cas/oidc", claims.getSubject());
            assertNull(claims.getClaim("authority_hints"));
            assertNotNull(claims.getClaim("jwks"));
            val metadata = (Map) claims.getClaim("metadata");
            assertNotNull(metadata.get("federation_entity"));
            assertNull(metadata.get("openid_provider"));
        }
    }

    @Nested
    class OpenIdProviderFederationEndpointTests extends AbstractOidcOpenIdProviderFederationTests {

        @Test
        void verifyTaInvalidIssuer() throws Exception {
            mockMvc.perform(get(FEDERATION_ENDPOINT_URL)
                            .with(request -> {
                                request.setScheme("https");
                                request.setServerName("unknown.example.org");
                                request.setContextPath("/cas");
                                request.setServletPath("/cas");
                                request.setServerPort(443);
                                return request;
                            }))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST));
        }

        @Test
        void verifyTaOperation() throws Exception {
            val result = mockMvc.perform(get(FEDERATION_ENDPOINT_URL)
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(OidcWellKnownFederationEndpointController.ENTITY_STATEMENT_TYPE))
                    .andReturn();
            val jwt = SignedJWT.parse(result.getResponse().getContentAsString());
            val claims = jwt.getJWTClaimsSet();
            assertEquals("https://sso.example.org/cas/oidc", claims.getIssuer());
            assertEquals("https://sso.example.org/cas/oidc", claims.getSubject());
            assertNotNull(claims.getClaim("authority_hints"));
            assertNotNull(claims.getClaim("jwks"));
            val metadata = (Map) claims.getClaim("metadata");
            assertNotNull(metadata.get("federation_entity"));
            assertNotNull(metadata.get("openid_provider"));
        }
    }
}
