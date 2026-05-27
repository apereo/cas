package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.AbstractOidcIntermediateFederationTests;
import org.apereo.cas.oidc.federation.AbstractOidcTrustAnchorFederationTests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import com.nimbusds.jwt.SignedJWT;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import jakarta.servlet.ServletException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcFetchFederationEndpointControllerTests}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@Tag("OIDCWeb")
class OidcFetchFederationEndpointControllerTests {

    private static final String OP = "http://op";
    private static final String RP = "http://rp";
    private static final String RP_NO_METADATA = "http://rpnometadata";
    private static final String RP_NO_KEYS = "http://rpnokeys";
    private static final String INTERMEDIATE = "http://intermediate";

    private static final String FETCH_ENDPOINT_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.FETCH_FEDERATION_URL;

    @Nested
    class TrustAnchorFetchFederationTests extends AbstractOidcTrustAnchorFederationTests {

        @Test
        void verifyInvalidIssuer() throws Exception {
            mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=" + RP)
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
            assertNull(claims.getClaim("constraints"));
            val jwks = (Map) claims.getClaim("jwks");
            assertNotNull(jwks);
            val keys = (List) jwks.get("keys");
            assertEquals(2, keys.size());
            val metadata = (Map) claims.getClaim("metadata");
            assertNull(metadata.get("federation_entity"));
            assertNull(metadata.get("openid_provider"));
            assertNotNull(metadata.get("openid_relying_party"));
        }

        @Test
        void verifyRelyingPartyNoMetadata() throws Exception {
            val thrown = assertThrows(ServletException.class, () -> {
                mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=" + RP_NO_METADATA)
                        .with(withHttpRequestProcessor()));
            });
            assertTrue(thrown.getCause() instanceof IllegalArgumentException);
            assertEquals("No metadata defined for entity", thrown.getCause().getMessage());
        }

        @Test
        void verifyRelyingPartyNoKeys() throws Exception {
            val thrown = assertThrows(ServletException.class, () -> {
                mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=" + RP_NO_KEYS)
                        .with(withHttpRequestProcessor()));
            });
            assertTrue(thrown.getCause() instanceof IllegalArgumentException);
            assertEquals("No federation keys defined for entity", thrown.getCause().getMessage());
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
            assertNull(claims.getClaim("constraints"));
            val jwks = (Map) claims.getClaim("jwks");
            assertNotNull(jwks);
            val keys = (List) jwks.get("keys");
            assertEquals(2, keys.size());
            val metadata = (Map) claims.getClaim("metadata");
            assertNull(metadata.get("federation_entity"));
            assertNotNull(metadata.get("openid_provider"));
            assertNull(metadata.get("openid_relying_party"));
        }

        @Test
        void verifyIntermediate() throws Exception {
            val result = mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=" + INTERMEDIATE)
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(OidcConstants.ENTITY_STATEMENT_CONTENT_TYPE))
                    .andReturn();
            val jwt = SignedJWT.parse(result.getResponse().getContentAsString());
            val claims = jwt.getJWTClaimsSet();
            assertEquals("https://sso.example.org/cas/oidc", claims.getIssuer());
            assertEquals(INTERMEDIATE, claims.getSubject());
            assertNull(claims.getClaim("authority_hints"));
            assertNotNull(claims.getClaim("constraints"));
            val jwks = (Map) claims.getClaim("jwks");
            assertNotNull(jwks);
            val keys = (List) jwks.get("keys");
            assertEquals(2, keys.size());
            val metadata = (Map) claims.getClaim("metadata");
            assertNotNull(metadata.get("federation_entity"));
            assertNull(metadata.get("openid_provider"));
            assertNull(metadata.get("openid_relying_party"));
        }
    }

    @Nested
    class IntermediateFetchFederationTests extends AbstractOidcIntermediateFederationTests {

        @Test
        void verifyInvalidIssuer() throws Exception {
            mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=" + RP)
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
            assertNull(claims.getClaim("constraints"));
            val jwks = (Map) claims.getClaim("jwks");
            assertNotNull(jwks);
            val keys = (List) jwks.get("keys");
            assertEquals(2, keys.size());
            val metadata = (Map) claims.getClaim("metadata");
            assertNull(metadata.get("federation_entity"));
            assertNull(metadata.get("openid_provider"));
            assertNotNull(metadata.get("openid_relying_party"));
        }

        @Test
        void verifyRelyingPartyNoMetadata() throws Exception {
            val thrown = assertThrows(ServletException.class, () -> {
                mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=" + RP_NO_METADATA)
                        .with(withHttpRequestProcessor()));
            });
            assertTrue(thrown.getCause() instanceof IllegalArgumentException);
            assertEquals("No metadata defined for entity", thrown.getCause().getMessage());
        }

        @Test
        void verifyRelyingPartyNoKeys() throws Exception {
            val thrown = assertThrows(ServletException.class, () -> {
                mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=" + RP_NO_KEYS)
                        .with(withHttpRequestProcessor()));
            });
            assertTrue(thrown.getCause() instanceof IllegalArgumentException);
            assertEquals("No federation keys defined for entity", thrown.getCause().getMessage());
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
            assertNull(claims.getClaim("constraints"));
            val jwks = (Map) claims.getClaim("jwks");
            assertNotNull(jwks);
            val keys = (List) jwks.get("keys");
            assertEquals(2, keys.size());
            val metadata = (Map) claims.getClaim("metadata");
            assertNull(metadata.get("federation_entity"));
            assertNotNull(metadata.get("openid_provider"));
            assertNull(metadata.get("openid_relying_party"));
        }

        @Test
        void verifyIntermediate() throws Exception {
            val result = mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=" + INTERMEDIATE)
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(OidcConstants.ENTITY_STATEMENT_CONTENT_TYPE))
                    .andReturn();
            val jwt = SignedJWT.parse(result.getResponse().getContentAsString());
            val claims = jwt.getJWTClaimsSet();
            assertEquals("https://sso.example.org/cas/oidc", claims.getIssuer());
            assertEquals(INTERMEDIATE, claims.getSubject());
            assertNull(claims.getClaim("authority_hints"));
            assertNotNull(claims.getClaim("constraints"));
            val jwks = (Map) claims.getClaim("jwks");
            assertNotNull(jwks);
            val keys = (List) jwks.get("keys");
            assertEquals(2, keys.size());
            val metadata = (Map) claims.getClaim("metadata");
            assertNotNull(metadata.get("federation_entity"));
            assertNull(metadata.get("openid_provider"));
            assertNull(metadata.get("openid_relying_party"));
        }
    }
}
