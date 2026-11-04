package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.AbstractOidcIntermediateFederationTests;
import org.apereo.cas.oidc.federation.AbstractOidcOpenIdProviderFederationTests;
import org.apereo.cas.oidc.federation.AbstractOidcTrustAnchorFederationTests;
import org.apereo.cas.oidc.federation.signature.OidcFederationJsonWebKeystoreService;
import org.apereo.cas.oidc.federation.subordinate.OidcFederationSubordinate;
import org.apereo.cas.oidc.federation.subordinate.OidcFederationSubordinateRepository;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.function.FunctionUtils;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatementClaimsSet;
import com.nimbusds.openid.connect.sdk.federation.trust.EntityStatementRetriever;
import com.nimbusds.openid.connect.sdk.federation.trust.ResolveException;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
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

        /**
         * A subordinate statement may carry a metadata policy instead of metadata (OpenID Federation 1.0, section 6.1),
         * with the non-standard operators that must be understood.
         */
        @Test
        void verifyRelyingPartyNoMetadata() throws Exception {
            val result = mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=" + RP_NO_METADATA)
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isOk())
                    .andReturn();
            val claims = SignedJWT.parse(result.getResponse().getContentAsString()).getJWTClaimsSet();
            assertNull(claims.getClaim("metadata"));
            val policy = (Map) ((Map) claims.getClaim("metadata_policy")).get("openid_relying_party");
            assertEquals(List.of("openid", "email"), ((Map) policy.get("scope")).get("subset_of"));
            assertEquals("^[A-Za-z ]+$", ((Map) policy.get("client_name")).get("regexp"));
            assertEquals(List.of("regexp"), claims.getStringListClaim("metadata_policy_crit"));
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
            assertEquals(2L, ((Map) claims.getClaim("constraints")).get("max_path_length"));
            val jwks = (Map) claims.getClaim("jwks");
            assertNotNull(jwks);
            val keys = (List<Map>) jwks.get("keys");
            assertEquals(1, keys.size());
            val metadata = (Map) claims.getClaim("metadata");
            val federationEntity = (Map) metadata.get("federation_entity");
            assertEquals("Intermediate", federationEntity.get("organization_name"));
            assertNull(federationEntity.get("federation_fetch_endpoint"));
            assertNull(federationEntity.get("federation_list_endpoint"));
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

        /**
         * A subordinate statement may carry a metadata policy instead of metadata (OpenID Federation 1.0, section 6.1),
         * with the non-standard operators that must be understood.
         */
        @Test
        void verifyRelyingPartyNoMetadata() throws Exception {
            val result = mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=" + RP_NO_METADATA)
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isOk())
                    .andReturn();
            val claims = SignedJWT.parse(result.getResponse().getContentAsString()).getJWTClaimsSet();
            assertNull(claims.getClaim("metadata"));
            val policy = (Map) ((Map) claims.getClaim("metadata_policy")).get("openid_relying_party");
            assertEquals(List.of("openid", "email"), ((Map) policy.get("scope")).get("subset_of"));
            assertEquals("^[A-Za-z ]+$", ((Map) policy.get("client_name")).get("regexp"));
            assertEquals(List.of("regexp"), claims.getStringListClaim("metadata_policy_crit"));
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
            assertEquals(2L, ((Map) claims.getClaim("constraints")).get("max_path_length"));
            val jwks = (Map) claims.getClaim("jwks");
            assertNotNull(jwks);
            val keys = (List<Map>) jwks.get("keys");
            assertEquals(1, keys.size());
            val metadata = (Map) claims.getClaim("metadata");
            val federationEntity = (Map) metadata.get("federation_entity");
            assertEquals("Intermediate", federationEntity.get("organization_name"));
            assertNull(federationEntity.get("federation_fetch_endpoint"));
            assertNull(federationEntity.get("federation_list_endpoint"));
            assertNull(metadata.get("openid_provider"));
            assertNull(metadata.get("openid_relying_party"));
        }
    }

    @Nested
    class OpenIdProviderFetchFederationTests extends AbstractOidcOpenIdProviderFederationTests {
        @Autowired
        @Qualifier("oidcFetchFederationController")
        private OidcFetchFederationEndpointController fetchController;

        @Autowired
        @Qualifier("oidcResolveFederationController")
        private OidcResolveFederationEndpointController resolveController;

        @Test
        void verifyRoleWithoutSubordinates() throws Exception {
            val result = fetchController.fetchEntityStatement(RP, new MockHttpServletRequest(), new MockHttpServletResponse());
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
            assertEquals("not_found", ((Map) result.getBody()).get("error"));
        }

        @Test
        void verifyRoleWithoutResolveEndpoint() throws Exception {
            val result = resolveController.resolveEntity("https://rp.example.org", List.of("https://trustanchor"), List.of(),
                new MockHttpServletRequest(), new MockHttpServletResponse());
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
            assertEquals("not_found", ((Map) result.getBody()).get("error"));
        }
    }

    /**
     * The resolve endpoint of a trust anchor (OpenID Federation 1.0, section 8.3): the trust chain of a subordinate is
     * resolved to the trust anchor, its own statements built locally, and its metadata resolved with the policies of the
     * chain. Entity configurations of other entities come from the retriever of these tests.
     */
    @Nested
    @Import(ResolveTrustAnchorFederationTests.ResolveTestConfiguration.class)
    class ResolveTrustAnchorFederationTests extends AbstractOidcTrustAnchorFederationTests {
        private static final String RESOLVE_ENDPOINT_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + OidcConstants.RESOLVE_FEDERATION_URL;

        private static final String WELL_KNOWN_ENDPOINT_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.WELL_KNOWN_OPENID_FEDERATION_URL;

        private static final String RELYING_PARTY = "https://rp.resolve.example.org";

        private static final String NATIVE_RELYING_PARTY = "https://native.resolve.example.org";

        private static final String UNREACHABLE = "https://unreachable.resolve.example.org";

        private static final RSAKey LEAF_KEY = FunctionUtils.doUnchecked(() -> new RSAKeyGenerator(2048).keyID("leaf-federation").generate());

        @Autowired
        @Qualifier("oidcFederationSubordinateRepository")
        private OidcFederationSubordinateRepository subordinateRepository;

        @Autowired
        @Qualifier("oidcFederationEntityStatementRetriever")
        private EntityStatementRetriever entityStatementRetriever;

        @Autowired
        @Qualifier(OidcFederationJsonWebKeystoreService.BEAN_NAME)
        private OidcFederationJsonWebKeystoreService keystoreService;

        @BeforeEach
        void registerSubordinates() {
            val mapper = JsonMapper.builder().build();
            val federationKeys = mapper.readTree('[' + LEAF_KEY.toPublicJWK().toJSONString() + ']');
            val metadataPolicy = mapper.readTree("""
                {"openid_relying_party": {"scope": {"subset_of": ["openid", "profile"]}, "application_type": {"one_of": ["web"]}}}
                """);
            for (val entityId : List.of(RELYING_PARTY, NATIVE_RELYING_PARTY, UNREACHABLE)) {
                subordinateRepository.getSubordinates().put(entityId, new OidcFederationSubordinate()
                    .setEntityId(entityId).setFederationKeys(federationKeys).setMetadataPolicy(metadataPolicy));
            }
        }

        @Test
        void verifyResolveEndpointIsPublished() throws Exception {
            val result = mockMvc.perform(get(WELL_KNOWN_ENDPOINT_URL).with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andReturn();
            val metadata = (Map) SignedJWT.parse(result.getResponse().getContentAsString()).getJWTClaimsSet().getClaim("metadata");
            assertEquals(ISSUER + OidcConstants.RESOLVE_FEDERATION_URL, ((Map) metadata.get("federation_entity")).get("federation_resolve_endpoint"));
        }

        @Test
        void verifySubordinateIsResolved() throws Exception {
            val result = mockMvc.perform(get(RESOLVE_ENDPOINT_URL)
                    .param("sub", RELYING_PARTY)
                    .param("trust_anchor", "https://other.example.org")
                    .param("trust_anchor", ISSUER)
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(OidcConstants.RESOLVE_RESPONSE_CONTENT_TYPE))
                .andReturn();
            val jwt = SignedJWT.parse(result.getResponse().getContentAsString());
            assertEquals("resolve-response+jwt", jwt.getHeader().getType().getType());
            val signingKey = keystoreService.toJWKSet().getKeyByKeyId(jwt.getHeader().getKeyID());
            val publicKey = signingKey instanceof final RSAKey rsaKey ? rsaKey.toPublicKey() : signingKey.toECKey().toPublicKey();
            assertTrue(jwt.verify(new DefaultJWSVerifierFactory().createJWSVerifier(jwt.getHeader(), publicKey)));

            val claims = jwt.getJWTClaimsSet();
            assertEquals(ISSUER, claims.getIssuer());
            assertEquals(RELYING_PARTY, claims.getSubject());
            assertNull(claims.getClaim("aud"));
            assertNull(claims.getClaim("trust_marks"));
            assertTrue(claims.getExpirationTime().after(claims.getIssueTime()));
            val metadata = (Map) claims.getClaim("metadata");
            assertEquals("openid profile", ((Map) metadata.get("openid_relying_party")).get("scope"));
            assertEquals("Resolved RP", ((Map) metadata.get("federation_entity")).get("organization_name"));

            val trustChain = claims.getStringListClaim("trust_chain");
            assertEquals(3, trustChain.size());
            assertEquals(RELYING_PARTY, SignedJWT.parse(trustChain.getFirst()).getJWTClaimsSet().getSubject());
            val subordinateStatement = SignedJWT.parse(trustChain.get(1)).getJWTClaimsSet();
            assertEquals(ISSUER, subordinateStatement.getIssuer());
            assertEquals(RELYING_PARTY, subordinateStatement.getSubject());
            val trustAnchorConfiguration = SignedJWT.parse(trustChain.getLast()).getJWTClaimsSet();
            assertEquals(ISSUER, trustAnchorConfiguration.getIssuer());
            assertEquals(ISSUER, trustAnchorConfiguration.getSubject());
            assertFalse(claims.getExpirationTime().after(SignedJWT.parse(trustChain.getFirst()).getJWTClaimsSet().getExpirationTime()));

            val federationEntityOnly = mockMvc.perform(get(RESOLVE_ENDPOINT_URL)
                    .param("sub", RELYING_PARTY)
                    .param("trust_anchor", ISSUER)
                    .param("entity_type", "federation_entity")
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andReturn();
            val federationEntityMetadata = (Map) SignedJWT.parse(federationEntityOnly.getResponse().getContentAsString())
                .getJWTClaimsSet().getClaim("metadata");
            assertEquals(Set.of("federation_entity"), federationEntityMetadata.keySet());
        }

        @Test
        void verifyErrors() throws Exception {
            mockMvc.perform(get(RESOLVE_ENDPOINT_URL).param("sub", RELYING_PARTY).with(withHttpRequestProcessor()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST));
            mockMvc.perform(get(RESOLVE_ENDPOINT_URL).param("sub", "http://rp.resolve.example.org").param("trust_anchor", ISSUER)
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST));
            mockMvc.perform(get(RESOLVE_ENDPOINT_URL).param("sub", RELYING_PARTY).param("trust_anchor", "https://other.example.org")
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("invalid_trust_anchor"));
            mockMvc.perform(get(RESOLVE_ENDPOINT_URL).param("sub", NATIVE_RELYING_PARTY).param("trust_anchor", ISSUER)
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_metadata"));
        }

        /**
         * A subject that cannot be resolved is remembered, so that asking again does not resolve its trust chains again.
         */
        @Test
        void verifyUnresolvedSubjectIsRemembered() throws Exception {
            for (var attempt = 0; attempt < 2; attempt++) {
                mockMvc.perform(get(RESOLVE_ENDPOINT_URL).param("sub", UNREACHABLE).param("trust_anchor", ISSUER)
                        .with(withHttpRequestProcessor()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("invalid_trust_chain"));
            }
            verify(entityStatementRetriever, times(1)).fetchEntityConfiguration(new EntityID(UNREACHABLE));
        }

        private static EntityStatement leafConfiguration(final String entityId, final String applicationType) throws Exception {
            val now = Instant.now(Clock.systemUTC());
            val claims = new EntityStatementClaimsSet(new EntityID(entityId), new EntityID(entityId),
                Date.from(now.minusSeconds(60)), Date.from(now.plusSeconds(86400)), new JWKSet(LEAF_KEY.toPublicJWK()));
            claims.setAuthorityHints(List.of(new EntityID(ISSUER)));
            claims.setClaim("metadata", Map.of(
                "openid_relying_party", Map.of("client_name", "Resolved RP", "scope", "openid profile email",
                    "application_type", applicationType, "redirect_uris", List.of(entityId + "/callback")),
                "federation_entity", Map.of("organization_name", "Resolved RP")));
            return EntityStatement.sign(claims, LEAF_KEY);
        }

        @TestConfiguration(value = "ResolveTestConfiguration", proxyBeanMethods = false)
        static class ResolveTestConfiguration {
            @Bean
            public EntityStatementRetriever oidcFederationEntityStatementRetriever() throws Exception {
                val retriever = mock(EntityStatementRetriever.class);
                when(retriever.fetchEntityConfiguration(any())).thenAnswer(invocation -> {
                    val target = invocation.getArgument(0, EntityID.class).getValue();
                    return switch (target) {
                        case RELYING_PARTY -> leafConfiguration(RELYING_PARTY, "web");
                        case NATIVE_RELYING_PARTY -> leafConfiguration(NATIVE_RELYING_PARTY, "native");
                        default -> throw new ResolveException("Not reachable: " + target);
                    };
                });
                return retriever;
            }
        }
    }
}
