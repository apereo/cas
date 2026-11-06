package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.AbstractOidcIntermediateFederationTests;
import org.apereo.cas.oidc.federation.AbstractOidcOpenIdProviderFederationTests;
import org.apereo.cas.oidc.federation.AbstractOidcTrustAnchorFederationTests;
import org.apereo.cas.oidc.federation.chain.OidcFederationResolutionLimiter;
import org.apereo.cas.oidc.federation.chain.OidcFederationResolutionUnavailableException;
import org.apereo.cas.oidc.federation.signature.OidcFederationJsonWebKeystoreService;
import org.apereo.cas.oidc.federation.subordinate.OidcFederationSubordinate;
import org.apereo.cas.oidc.federation.subordinate.OidcFederationSubordinateRepository;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.function.FunctionUtils;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

        /**
         * Errors are JSON (OpenID Federation 1.0, section 8.9) even when the request only accepts entity statements.
         */
        @Test
        void verifyErrorIsJsonWhateverIsAccepted() throws Exception {
            mockMvc.perform(get(FETCH_ENDPOINT_URL + "?sub=http://fake")
                            .accept(OidcConstants.ENTITY_STATEMENT_CONTENT_TYPE)
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").value("not_found"));
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

        private static final String UNAVAILABLE_ONCE = "https://unavailable.resolve.example.org";

        private static final String OUT_OF_TIME = "https://out-of-time.resolve.example.org";

        private static final String CONSTRAINED_OUT_OF_TIME = "https://constrained.resolve.example.org";

        private static final AtomicInteger UNAVAILABLE_ATTEMPTS = new AtomicInteger();

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
            registerResolvableSubordinates(subordinateRepository);
        }

        static void registerResolvableSubordinates(final OidcFederationSubordinateRepository subordinateRepository) {
            val mapper = JsonMapper.builder().build();
            val federationKeys = mapper.readTree('[' + LEAF_KEY.toPublicJWK().toJSONString() + ']');
            val metadataPolicy = mapper.readTree("""
                {"openid_relying_party": {"scope": {"subset_of": ["openid", "profile"]}, "application_type": {"one_of": ["web"]}}}
                """);
            for (val entityId : List.of(RELYING_PARTY, NATIVE_RELYING_PARTY, UNREACHABLE)) {
                subordinateRepository.getSubordinates().put(entityId, new OidcFederationSubordinate()
                    .setEntityId(entityId).setFederationKeys(federationKeys).setMetadataPolicy(metadataPolicy));
            }
            subordinateRepository.getSubordinates().put(CONSTRAINED_OUT_OF_TIME, new OidcFederationSubordinate()
                .setEntityId(CONSTRAINED_OUT_OF_TIME).setFederationKeys(federationKeys).setMetadataPolicy(metadataPolicy)
                .setConstraints(mapper.readTree("""
                    {"naming_constraints": {"excluded": ["constrained.resolve.example.org"]}}
                    """)));
        }

        @Test
        void verifyResolveEndpointIsPublished() throws Exception {
            val result = mockMvc.perform(get(WELL_KNOWN_ENDPOINT_URL).with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andReturn();
            val metadata = (Map) SignedJWT.parse(result.getResponse().getContentAsString()).getJWTClaimsSet().getClaim("metadata");
            assertEquals(ISSUER + OidcConstants.RESOLVE_FEDERATION_URL, ((Map) metadata.get("federation_entity")).get("federation_resolve_endpoint"));
            assertNull(((Map) metadata.get("federation_entity")).get("federation_resolve_endpoint_auth_methods"));
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
            mockMvc.perform(post(RESOLVE_ENDPOINT_URL).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("sub", RELYING_PARTY).param("trust_anchor", ISSUER).with(withHttpRequestProcessor()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST))
                .andExpect(jsonPath("$.error_description").value("Client authentication is not supported"));
            mockMvc.perform(get(RESOLVE_ENDPOINT_URL).param("sub", RELYING_PARTY)
                    .accept(OidcConstants.RESOLVE_RESPONSE_CONTENT_TYPE).with(withHttpRequestProcessor()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
         * A subordinate definition whose metadata, metadata policy or constraints are not objects, or whose critical
         * operators are null, is answered with a JSON server error instead of failing the request.
         */
        @Test
        void verifyMalformedSubordinatesAnswerServerError() throws Exception {
            val mapper = JsonMapper.builder().build();
            val keys = mapper.readTree('[' + LEAF_KEY.toPublicJWK().toJSONString() + ']');
            val malformed = Map.of(
                "https://array-metadata.resolve.example.org", new OidcFederationSubordinate().setMetadata(mapper.readTree("[]")),
                "https://string-policy.resolve.example.org", new OidcFederationSubordinate().setMetadataPolicy(mapper.readTree("\"policy\"")),
                "https://array-constraints.resolve.example.org", new OidcFederationSubordinate().setConstraints(mapper.readTree("[1]")));
            malformed.forEach((entityId, subordinate) -> {
                subordinateRepository.getSubordinates().put(entityId, subordinate.setEntityId(entityId).setFederationKeys(keys));
                FunctionUtils.doUnchecked(_ -> mockMvc.perform(get(FETCH_ENDPOINT_URL).param("sub", entityId).with(withHttpRequestProcessor()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").value("server_error")));
            });
            val withoutCrit = "https://null-crit.resolve.example.org";
            subordinateRepository.getSubordinates().put(withoutCrit, new OidcFederationSubordinate().setEntityId(withoutCrit)
                .setFederationKeys(keys).setMetadataPolicyCrit(null).setMetadata(mapper.readTree("null")));
            mockMvc.perform(get(FETCH_ENDPOINT_URL).param("sub", withoutCrit).with(withHttpRequestProcessor()))
                .andExpect(status().isOk());
        }

        /**
         * The listing filters by the entity types of a subordinate as its definition declares them, or else by its
         * metadata: the subordinates of these tests only carry a metadata policy for relying parties, which does not
         * make them relying parties.
         */
        @Test
        void verifyListingFiltersByEntityTypes() throws Exception {
            val response = mockMvc.perform(get("/cas/" + OidcConstants.BASE_OIDC_URL + OidcConstants.LIST_FEDERATION_URL)
                    .param("entity_type", "openid_relying_party")
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
            val listing = JsonMapper.builder().build().readValue(response, List.class);
            assertFalse(listing.contains(RELYING_PARTY), response);
            assertTrue(listing.contains(RP), response);
            assertTrue(listing.contains(RP_NO_METADATA), response);
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

        /**
         * A resolution that is unavailable, such as when a trust anchor cannot be reached for now, is answered with
         * {@code 503} and not remembered, so that the subject is resolved again with the next request.
         */
        @Test
        void verifyUnavailableResolutionIsNotRemembered() throws Exception {
            UNAVAILABLE_ATTEMPTS.set(0);
            val subject = UNAVAILABLE_ONCE + '/' + UUID.randomUUID();
            mockMvc.perform(get(RESOLVE_ENDPOINT_URL).param("sub", subject).param("trust_anchor", ISSUER)
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("temporarily_unavailable"));
            mockMvc.perform(get(RESOLVE_ENDPOINT_URL).param("sub", subject).param("trust_anchor", ISSUER)
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_trust_chain"));
            assertEquals(2, UNAVAILABLE_ATTEMPTS.get());
        }

        /**
         * A resolution whose request to a trust anchor ran out of time, or found no room among the running requests,
         * cannot tell whether the subject has a trust chain to it: it is answered with {@code 503} and not remembered,
         * whether the resolution failed or every chain it found was refused by the constraints of its statements.
         */
        @Test
        void verifyResolutionOutOfTimeIsNotRemembered() throws Exception {
            val subject = OUT_OF_TIME + '/' + UUID.randomUUID();
            for (var attempt = 0; attempt < 2; attempt++) {
                mockMvc.perform(get(RESOLVE_ENDPOINT_URL).param("sub", subject).param("trust_anchor", ISSUER)
                        .with(withHttpRequestProcessor()))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error").value("temporarily_unavailable"));
            }
            verify(entityStatementRetriever, times(2)).fetchEntityConfiguration(new EntityID(subject));

            for (var attempt = 0; attempt < 2; attempt++) {
                mockMvc.perform(get(RESOLVE_ENDPOINT_URL).param("sub", CONSTRAINED_OUT_OF_TIME).param("trust_anchor", ISSUER)
                        .with(withHttpRequestProcessor()))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error").value("temporarily_unavailable"));
            }
        }

        static EntityStatement leafConfiguration(final String entityId, final String applicationType) throws Exception {
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
                        case CONSTRAINED_OUT_OF_TIME -> {
                            OidcFederationResolutionLimiter.currentBudget().ifPresent(OidcFederationResolutionLimiter.RequestBudget::markIndeterminate);
                            yield leafConfiguration(CONSTRAINED_OUT_OF_TIME, "web");
                        }
                        default -> {
                            if (target.startsWith(UNAVAILABLE_ONCE) && UNAVAILABLE_ATTEMPTS.incrementAndGet() == 1) {
                                throw new OidcFederationResolutionUnavailableException("Not reachable for now: " + target);
                            }
                            if (target.startsWith(OUT_OF_TIME)) {
                                OidcFederationResolutionLimiter.currentBudget().ifPresent(OidcFederationResolutionLimiter.RequestBudget::markIndeterminate);
                            }
                            throw new ResolveException("Not reachable: " + target);
                        }
                    };
                });
                return retriever;
            }
        }
    }

    /**
     * Client authentication at the resolve endpoint (OpenID Federation 1.0, section 8.8), required: only a subordinate
     * authenticated with {@code private_key_jwt}, once per client assertion, gets a resolve response, addressed to it.
     */
    @SpringBootTest(
        classes = AbstractOidcTrustAnchorFederationTests.SharedTestConfiguration.class,
        properties = {
            "cas.server.name=https://sso.example.org/",
            "cas.server.prefix=https://sso.example.org/cas",
            "cas.authn.oidc.core.issuer=https://sso.example.org/cas/oidc",
            "cas.authn.oidc.federation.role=TRUST_ANCHOR",
            "cas.authn.oidc.federation.jwks-file=file:${#systemProperties['java.io.tmpdir']}/authenticated-resolve-federation.jwks",
            "cas.authn.oidc.federation.subordinate-directory=./src/test/resources/subordinates",
            "cas.authn.oidc.federation.resolve-endpoint-authentication=REQUIRED"
        }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Nested
    @Import(ResolveTrustAnchorFederationTests.ResolveTestConfiguration.class)
    class AuthenticatedResolveTrustAnchorFederationTests extends AbstractOidcTrustAnchorFederationTests {
        private static final String RESOLVE_ENDPOINT_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + OidcConstants.RESOLVE_FEDERATION_URL;

        private static final String REQUESTER = "https://requester.resolve.example.org";

        private static final RSAKey REQUESTER_KEY = FunctionUtils.doUnchecked(() -> new RSAKeyGenerator(2048).keyID("requester-federation").generate());

        @Autowired
        @Qualifier("oidcFederationSubordinateRepository")
        private OidcFederationSubordinateRepository subordinateRepository;

        @BeforeEach
        void registerSubordinates() {
            ResolveTrustAnchorFederationTests.registerResolvableSubordinates(subordinateRepository);
            val federationKeys = JsonMapper.builder().build().readTree('[' + REQUESTER_KEY.toPublicJWK().toJSONString() + ']');
            subordinateRepository.getSubordinates().put(REQUESTER,
                new OidcFederationSubordinate().setEntityId(REQUESTER).setFederationKeys(federationKeys));
        }

        @Test
        void verifyOnlyAuthenticatedSubordinateResolves() throws Exception {
            val configuration = mockMvc.perform(get("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.WELL_KNOWN_OPENID_FEDERATION_URL)
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andReturn();
            val metadata = (Map) SignedJWT.parse(configuration.getResponse().getContentAsString()).getJWTClaimsSet().getClaim("metadata");
            val federationEntity = (Map) metadata.get("federation_entity");
            assertEquals(List.of("private_key_jwt"), federationEntity.get("federation_resolve_endpoint_auth_methods"));
            assertNotNull(federationEntity.get("endpoint_auth_signing_alg_values_supported"));

            mockMvc.perform(get(RESOLVE_ENDPOINT_URL).param("sub", ResolveTrustAnchorFederationTests.RELYING_PARTY)
                    .param("trust_anchor", ISSUER).with(withHttpRequestProcessor()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_client"));

            val assertion = clientAssertion(REQUESTER_KEY, REQUESTER, List.of(ISSUER));
            val result = mockMvc.perform(authenticatedRequest(assertion))
                .andExpect(status().isOk())
                .andExpect(content().contentType(OidcConstants.RESOLVE_RESPONSE_CONTENT_TYPE))
                .andReturn();
            val claims = SignedJWT.parse(result.getResponse().getContentAsString()).getJWTClaimsSet();
            assertEquals(ResolveTrustAnchorFederationTests.RELYING_PARTY, claims.getSubject());
            assertEquals(List.of(REQUESTER), claims.getAudience());

            mockMvc.perform(authenticatedRequest(assertion))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_client"));
            mockMvc.perform(authenticatedRequest(clientAssertion(REQUESTER_KEY, REQUESTER, List.of(ISSUER, "https://other.example.org"))))
                .andExpect(status().isUnauthorized());
            mockMvc.perform(authenticatedRequest(clientAssertion(
                    FunctionUtils.doUnchecked(() -> new RSAKeyGenerator(2048).keyID("requester-federation").generate()), REQUESTER, List.of(ISSUER))))
                .andExpect(status().isUnauthorized());
            mockMvc.perform(post(RESOLVE_ENDPOINT_URL).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("sub", ResolveTrustAnchorFederationTests.RELYING_PARTY).param("trust_anchor", ISSUER)
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isUnauthorized());
        }

        private MockHttpServletRequestBuilder authenticatedRequest(final String clientAssertion) {
            return post(RESOLVE_ENDPOINT_URL).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("sub", ResolveTrustAnchorFederationTests.RELYING_PARTY)
                .param("trust_anchor", ISSUER)
                .param(OAuth20Constants.CLIENT_ASSERTION_TYPE, OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER)
                .param(OAuth20Constants.CLIENT_ASSERTION, clientAssertion)
                .with(withHttpRequestProcessor());
        }

        private static String clientAssertion(final RSAKey key, final String issuer, final List<String> audience) throws Exception {
            val claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(issuer)
                .audience(audience)
                .jwtID(UUID.randomUUID().toString())
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(60)))
                .build();
            val jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(), claims);
            jwt.sign(new RSASSASigner(key));
            return jwt.serialize();
        }
    }
}
