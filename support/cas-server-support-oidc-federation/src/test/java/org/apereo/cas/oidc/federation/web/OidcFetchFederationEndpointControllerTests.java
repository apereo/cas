package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.AbstractOidcIntermediateFederationTests;
import org.apereo.cas.oidc.federation.AbstractOidcOpenIdProviderFederationTests;
import org.apereo.cas.oidc.federation.AbstractOidcTrustAnchorFederationTests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import com.nimbusds.jwt.SignedJWT;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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

    private static final String FETCH_ENDPOINT_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + OidcConstants.FETCH_FEDERATION_URL;

    private static final String ISSUER = "https://sso.example.org/cas/oidc";

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
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("not_found"))
                    .andExpect(jsonPath("$.error_description").value("Unknown subordinate entity"));
        }

        @Test
        void verifyOwnEntity() throws Exception {
            mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=" + ISSUER)
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
            val keys = (List<Map>) jwks.get("keys");
            assertEquals(1, keys.size());
            assertEquals("TJns", keys.getFirst().get("kid"));
            assertEquals(Duration.ofDays(90).toMillis(), claims.getExpirationTime().getTime() - claims.getIssueTime().getTime());
            assertTrue(Duration.between(claims.getIssueTime().toInstant(), Instant.now()).abs().toMinutes() < 1);
            val metadata = (Map) claims.getClaim("metadata");
            assertNull(metadata.get("federation_entity"));
            assertNull(metadata.get("openid_provider"));
            assertNotNull(metadata.get("openid_relying_party"));
        }

        @Test
        void verifyRelyingPartyNoMetadata() throws Exception {
            mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=" + RP_NO_METADATA)
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("server_error"))
                    .andExpect(jsonPath("$.error_description").value("Subordinate entity is not defined correctly"));
        }

        @Test
        void verifyRelyingPartyNoKeys() throws Exception {
            mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=" + RP_NO_KEYS)
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("server_error"))
                    .andExpect(jsonPath("$.error_description").value("Subordinate entity is not defined correctly"));
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
            val keys = (List<Map>) jwks.get("keys");
            assertEquals(1, keys.size());
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
            val keys = (List<Map>) jwks.get("keys");
            assertEquals(1, keys.size());
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
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("not_found"))
                    .andExpect(jsonPath("$.error_description").value("Unknown subordinate entity"));
        }

        @Test
        void verifyOwnEntity() throws Exception {
            mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=" + ISSUER)
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
            val keys = (List<Map>) jwks.get("keys");
            assertEquals(1, keys.size());
            assertEquals("TJns", keys.getFirst().get("kid"));
            assertEquals(Duration.ofDays(90).toMillis(), claims.getExpirationTime().getTime() - claims.getIssueTime().getTime());
            assertTrue(Duration.between(claims.getIssueTime().toInstant(), Instant.now()).abs().toMinutes() < 1);
            val metadata = (Map) claims.getClaim("metadata");
            assertNull(metadata.get("federation_entity"));
            assertNull(metadata.get("openid_provider"));
            assertNotNull(metadata.get("openid_relying_party"));
        }

        @Test
        void verifyRelyingPartyNoMetadata() throws Exception {
            mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=" + RP_NO_METADATA)
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("server_error"))
                    .andExpect(jsonPath("$.error_description").value("Subordinate entity is not defined correctly"));
        }

        @Test
        void verifyRelyingPartyNoKeys() throws Exception {
            mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=" + RP_NO_KEYS)
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("server_error"))
                    .andExpect(jsonPath("$.error_description").value("Subordinate entity is not defined correctly"));
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
            val keys = (List<Map>) jwks.get("keys");
            assertEquals(1, keys.size());
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
            val keys = (List<Map>) jwks.get("keys");
            assertEquals(1, keys.size());
            val metadata = (Map) claims.getClaim("metadata");
            assertNotNull(metadata.get("federation_entity"));
            assertNull(metadata.get("openid_provider"));
            assertNull(metadata.get("openid_relying_party"));
        }
    }

    @Nested
    class OpenIdProviderFetchFederationTests extends AbstractOidcOpenIdProviderFederationTests {
        @Autowired
        @Qualifier("oidcFetchFederationController")
        private OidcFetchFederationEndpointController fetchController;

        @Test
        void verifyRoleWithoutSubordinates() throws Exception {
            val result = fetchController.fetchEntityStatement(RP, new MockHttpServletRequest(), new MockHttpServletResponse());
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
            assertEquals("not_found", ((Map) result.getBody()).get("error"));
        }
    }
}
