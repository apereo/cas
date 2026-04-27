package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.AbstractOidcOpenIdProviderFederationTests;
import org.apereo.cas.oidc.federation.AbstractOidcTrustAnchorFederationTests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import com.nimbusds.jwt.SignedJWT;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.servlet.ServletException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcWellKnownFederationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @author Jerome LELEU
 * @since 7.3.0
 */
@Tag("OIDCWeb")
class OidcWellKnownFederationEndpointControllerTests {

    private static final String FEDERATION_ENDPOINT_URL =
        "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.WELL_KNOWN_OPENID_FEDERATION_URL;

    @Nested
    class TrustAnchorFederationEndpointTests extends AbstractOidcTrustAnchorFederationTests {

        @Test
        void verifyInvalidIssuer() throws Exception {
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
                    .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST))
                    .andExpect(jsonPath("$.error_description").value("Invalid issuer"));
        }

        @Test
        void verifyOperation() throws Exception {
            val result = mockMvc.perform(get(FEDERATION_ENDPOINT_URL)
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(OidcConstants.ENTITY_STATEMENT_CONTENT_TYPE))
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
        void verifyInvalidIssuer() throws Exception {
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
                    .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST))
                    .andExpect(jsonPath("$.error_description").value("Invalid issuer"));
        }

        @Test
        void verifyOperation() throws Exception {
            val result = mockMvc.perform(get(FEDERATION_ENDPOINT_URL)
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(OidcConstants.ENTITY_STATEMENT_CONTENT_TYPE))
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

    @SpringBootTest(
        classes = AbstractOidcOpenIdProviderFederationTests.SharedTestConfiguration.class,
        properties = {
            "cas.server.name=https://sso.example.org/",
            "cas.server.prefix=https://sso.example.org/cas",
            "cas.authn.oidc.federation.role=TRUST_ANCHOR",
            "cas.authn.oidc.jwks.file-system.jwks-file=file:${#systemProperties['java.io.tmpdir']}/oidc.jwks",
            "cas.authn.oidc.federation.jwks-file=file:${#systemProperties['java.io.tmpdir']}/federation.jwks",
            "cas.authn.oidc.core.issuer=https://sso.example.org/cas/oidc"
        }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Nested
    class BadRoleOpenIdProviderFederationEndpointTests extends AbstractOidcOpenIdProviderFederationTests {

        @Test
        void verifyOperation() throws Exception {
            val thrown = assertThrows(ServletException.class, () -> {
                mockMvc.perform(get(FEDERATION_ENDPOINT_URL)
                        .with(withHttpRequestProcessor()));
            });
            assertTrue(thrown.getCause() instanceof IllegalArgumentException);
            assertEquals("Federation role [TRUST_ANCHOR] is not supported for OpenID Provider", thrown.getCause().getMessage());
        }
    }

    @SpringBootTest(
        classes = AbstractOidcTrustAnchorFederationTests.SharedTestConfiguration.class,
        properties = {
            "cas.server.name=https://sso.example.org/",
            "cas.server.prefix=https://sso.example.org/cas",
            "cas.authn.oidc.federation.role=OPENID_PROVIDER",
            "cas.authn.oidc.federation.jwks-file=file:${#systemProperties['java.io.tmpdir']}/federation.jwks",
            "cas.authn.oidc.core.issuer=https://sso.example.org/cas/oidc",
            "cas.service-registry.json.location=classpath:/services"
        }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Nested
    class BadRoleTrustAnchorFederationEndpointTests extends AbstractOidcTrustAnchorFederationTests {
        @Test
        void verifyOperation() throws Exception {
            val thrown = assertThrows(ServletException.class, () -> {
                mockMvc.perform(get(FEDERATION_ENDPOINT_URL)
                        .with(withHttpRequestProcessor()));
            });
            assertTrue(thrown.getCause() instanceof IllegalArgumentException);
            assertEquals("Federation role [OPENID_PROVIDER] is not supported for Trust Anchor/Intermediate", thrown.getCause().getMessage());
        }
    }
}
