package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.discovery.OidcFederationEntityMetadataContributor;
import org.apereo.cas.oidc.federation.AbstractOidcIntermediateFederationTests;
import org.apereo.cas.oidc.federation.AbstractOidcOpenIdProviderFederationTests;
import org.apereo.cas.oidc.federation.AbstractOidcTrustAnchorFederationTests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
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

    private static final String MEMBER_TRUST_MARK_TYPE = "https://sso.example.org/cas/oidc/trust-marks/member";

    private static final String TRUST_MARK_HEADER = "eyJhbGciOiJIUzI1NiIsInR5cCI6InRydXN0LW1hcmsrand0In0";

    private static final String CERTIFIED_TRUST_MARK = TRUST_MARK_HEADER + '.'
        + "eyJpc3MiOiJodHRwczovL3RydXN0LmV4YW1wbGUub3JnIiwic3ViIjoiaHR0cHM6Ly9zc28uZXhhbXBsZS5vcmcvY2FzL29pZGMiLCJ0cnVzdF9tYXJrX3R5"
        + "cGUiOiJodHRwczovL3RydXN0LmV4YW1wbGUub3JnL21hcmtzL2NlcnRpZmllZCIsImlhdCI6MTc5Mzc1MDQwMH0"
        + ".LlKGcrgGjoN-WDWMre7JkVkFxwFipNzENZ83Yj3qk7E";

    private static final String OTHER_ENTITY_TRUST_MARK = TRUST_MARK_HEADER + '.'
        + "eyJpc3MiOiJodHRwczovL3RydXN0LmV4YW1wbGUub3JnIiwic3ViIjoiaHR0cHM6Ly9vdGhlci5leGFtcGxlLm9yZyIsInRydXN0X21hcmtfdHlwZSI6Imh0"
        + "dHBzOi8vdHJ1c3QuZXhhbXBsZS5vcmcvbWFya3Mvb3RoZXIiLCJpYXQiOjE3OTM3NTA0MDB9"
        + ".HuSSH-pccfNOa1JLIB7HAhYw7XYWmdG8y46k31jLL-o";

    private static final String EXPIRED_TRUST_MARK = TRUST_MARK_HEADER + '.'
        + "eyJpc3MiOiJodHRwczovL3RydXN0LmV4YW1wbGUub3JnIiwic3ViIjoiaHR0cHM6Ly9zc28uZXhhbXBsZS5vcmcvY2FzL29pZGMiLCJ0cnVzdF9tYXJrX3R5"
        + "cGUiOiJodHRwczovL3RydXN0LmV4YW1wbGUub3JnL21hcmtzL2V4cGlyZWQiLCJpYXQiOjEwMDAwMDAwMDAsImV4cCI6MTAwMDAwMzYwMH0"
        + ".7F7XtZuiOAN1o7TQ4MgGJ3pL6qiW22wkcmLoLN0IFqc";

    private static final String UNTYPED_TRUST_MARK = TRUST_MARK_HEADER + '.'
        + "eyJpc3MiOiJodHRwczovL3RydXN0LmV4YW1wbGUub3JnIiwic3ViIjoiaHR0cHM6Ly9zc28uZXhhbXBsZS5vcmcvY2FzL29pZGMiLCJpYXQiOjE3OTM3NTA0"
        + "MDB9"
        + ".rXuWUMugLowLH2noTCBLRu7A25oP6gNTNlxy9aHU99k";

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
            assertNull(claims.getClaim("constraints"));
            assertNull(claims.getClaim("trust_marks"));
            assertNull(metadata.get("openid_provider"));
            assertEquals("https://sso.example.org/cas/oidc/federation_historical_keys",
                ((Map) metadata.get("federation_entity")).get("federation_historical_keys_endpoint"));
        }
    }

    @Nested
    class IntermediateFederationEndpointTests extends AbstractOidcIntermediateFederationTests {

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
            assertNull(claims.getClaim("constraints"));
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
            val openIdProvider = (Map) metadata.get("openid_provider");
            assertNotNull(openIdProvider);
            assertEquals("automatic", ((List<String>) openIdProvider.get("client_registration_types_supported")).getFirst());
            assertNull(metadata.get("openid_credential_issuer"));
            assertNull(claims.getClaim("trust_marks"));
            assertNull(claims.getClaim("trust_mark_issuers"));
            assertNull(claims.getClaim("trust_mark_owners"));
            assertNull(((Map) metadata.get("federation_entity")).get("federation_trust_mark_endpoint"));
            mockMvc.perform(get("/cas/" + OidcConstants.BASE_OIDC_URL + OidcConstants.TRUST_MARK_FEDERATION_URL)
                    .param("trust_mark_type", MEMBER_TRUST_MARK_TYPE).param("sub", "https://rp.example.org")
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
        }
    }

    @SpringBootTest(
        classes = {
            AbstractOidcOpenIdProviderFederationTests.SharedTestConfiguration.class,
            OidcWellKnownFederationEndpointControllerTests.MetadataContributorTestConfiguration.class
        },
        properties = {
            "cas.server.name=https://sso.example.org/",
            "cas.server.prefix=https://sso.example.org/cas",
            "cas.authn.oidc.core.issuer=https://sso.example.org/cas/oidc",
            "cas.authn.oidc.jwks.file-system.jwks-file=file:${#systemProperties['java.io.tmpdir']}/oidc.jwks",
            "cas.authn.oidc.federation.role=OPENID_PROVIDER",
            "cas.authn.oidc.federation.jwks-file=file:${#systemProperties['java.io.tmpdir']}/federation.jwks",
            "cas.authn.oidc.federation.authority-hints[0]=https://trustanchor",
            "cas.authn.oidc.federation.trust-marks[0]=" + CERTIFIED_TRUST_MARK,
            "cas.authn.oidc.federation.trust-marks[1]=classpath:trust-marks/member.jwt",
            "cas.authn.oidc.federation.trust-marks[2]=" + OTHER_ENTITY_TRUST_MARK,
            "cas.authn.oidc.federation.trust-marks[3]=" + EXPIRED_TRUST_MARK,
            "cas.authn.oidc.federation.trust-marks[4]=" + UNTYPED_TRUST_MARK,
            "cas.authn.oidc.federation.trust-marks[5]=classpath:trust-marks/missing.jwt"
        }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Nested
    class ContributedMetadataAndTrustMarksOpenIdProviderFederationTests extends AbstractOidcOpenIdProviderFederationTests {

        @Test
        void verifyContributedMetadata() throws Exception {
            val claims = fetchEntityConfiguration();
            val metadata = (Map<String, Object>) claims.getClaim("metadata");
            assertEquals(Set.of("openid_provider", "federation_entity", "openid_credential_issuer", "oauth_authorization_server"),
                metadata.keySet());
            val credentialIssuer = (Map<String, Object>) metadata.get("openid_credential_issuer");
            assertEquals("https://sso.example.org/cas/oidc", credentialIssuer.get("credential_issuer"));
            assertEquals(List.of("https://sso.example.org/cas/oidc"), credentialIssuer.get("authorization_servers"));
            val openIdProvider = (Map<String, Object>) metadata.get("openid_provider");
            assertEquals("https://sso.example.org/cas/oidc", openIdProvider.get("issuer"));
            assertFalse(openIdProvider.containsKey("contributed"));
            val federationEntity = (Map<String, Object>) metadata.get("federation_entity");
            assertFalse(federationEntity.containsKey("contributed"));
        }

        @Test
        void verifyTrustMarks() throws Exception {
            val claims = fetchEntityConfiguration();
            val trustMarks = (List<Map<String, Object>>) claims.getClaim("trust_marks");
            assertEquals(2, trustMarks.size());
            assertEquals("https://trust.example.org/marks/certified", trustMarks.getFirst().get("trust_mark_type"));
            assertEquals(CERTIFIED_TRUST_MARK, trustMarks.getFirst().get("trust_mark"));
            assertEquals("https://trust.example.org/marks/member", trustMarks.get(1).get("trust_mark_type"));
            val memberTrustMark = SignedJWT.parse((String) trustMarks.get(1).get("trust_mark")).getJWTClaimsSet();
            assertEquals("https://sso.example.org/cas/oidc", memberTrustMark.getSubject());
            assertEquals("https://trust.example.org/marks/member", memberTrustMark.getStringClaim("trust_mark_type"));
        }

        private JWTClaimsSet fetchEntityConfiguration() throws Exception {
            val result = mockMvc.perform(get(FEDERATION_ENDPOINT_URL)
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andReturn();
            return SignedJWT.parse(result.getResponse().getContentAsString()).getJWTClaimsSet();
        }
    }

    /**
     * Key rollover through the actuator endpoint, and the historical keys endpoint (OpenID Federation 1.0, section 8.7).
     */
    @SpringBootTest(
        classes = AbstractOidcTrustAnchorFederationTests.SharedTestConfiguration.class,
        properties = {
            "cas.server.name=https://sso.example.org/",
            "cas.server.prefix=https://sso.example.org/cas",
            "cas.authn.oidc.core.issuer=https://sso.example.org/cas/oidc",
            "cas.authn.oidc.federation.role=TRUST_ANCHOR",
            "cas.authn.oidc.federation.jwks-file=file:${#systemProperties['java.io.tmpdir']}/rollover-federation.jwks",
            "cas.authn.oidc.federation.subordinate-directory=./src/test/resources/subordinates",
            "cas.authn.oidc.federation.trust-mark-issuers[0].trust-mark-type=https://sso.example.org/cas/oidc/trust-marks/member",
            "cas.authn.oidc.federation.trust-mark-issuers[0].issuers[0]=https://sso.example.org/cas/oidc",
            "cas.authn.oidc.federation.trust-mark-issuers[1].trust-mark-type=https://refeds.org/sirtfi",
            "management.endpoints.web.exposure.include=oidcFederationJwks",
            "management.endpoints.access.default=UNRESTRICTED"
        }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Nested
    class KeyRolloverTrustAnchorFederationTests extends AbstractOidcTrustAnchorFederationTests {
        @Autowired
        @Qualifier("oidcFederationJwksEndpoint")
        private OidcFederationJwksEndpoint oidcFederationJwksEndpoint;

        @BeforeAll
        static void removeKeystore() throws Exception {
            Files.deleteIfExists(Path.of(FileUtils.getTempDirectoryPath(), "rollover-federation.jwks"));
        }

        @Test
        void verifyTrustMarkIssuersArePublished() throws Exception {
            val trustMarkIssuers = (Map<String, Object>) fetchEntityConfiguration().getJWTClaimsSet().getClaim("trust_mark_issuers");
            assertEquals(List.of("https://sso.example.org/cas/oidc"), trustMarkIssuers.get("https://sso.example.org/cas/oidc/trust-marks/member"));
            assertEquals(List.of(), trustMarkIssuers.get("https://refeds.org/sirtfi"));
        }

        @Test
        void verifyRolloverAndHistoricalKeys() throws Exception {
            val initial = fetchEntityConfiguration();
            val initialKeys = (Map<String, Object>) initial.getJWTClaimsSet().getClaim("jwks");
            assertEquals(2, ((List<?>) initialKeys.get("keys")).size());
            val retiringKey = initial.getHeader().getKeyID();

            oidcFederationJwksEndpoint.rotate();
            val rotated = fetchEntityConfiguration();
            assertNotEquals(retiringKey, rotated.getHeader().getKeyID());
            assertTrue(keyIds(rotated).contains(retiringKey));
            assertTrue(rotated.verify(new DefaultJWSVerifierFactory().createJWSVerifier(rotated.getHeader(),
                ((RSAKey) JWKSet.parse((Map<String, Object>) rotated.getJWTClaimsSet().getClaim("jwks"))
                    .getKeyByKeyId(rotated.getHeader().getKeyID())).toRSAPublicKey())));

            oidcFederationJwksEndpoint.revoke();
            val revoked = fetchEntityConfiguration();
            assertFalse(keyIds(revoked).contains(retiringKey));
            assertEquals(HttpStatus.OK, oidcFederationJwksEndpoint.keys().getStatusCode());
            assertEquals(HttpStatus.NOT_FOUND, oidcFederationJwksEndpoint.revokeKey("unknown", "compromised").getStatusCode());

            val result = mockMvc.perform(get("/cas/" + OidcConstants.BASE_OIDC_URL + OidcConstants.HISTORICAL_KEYS_FEDERATION_URL)
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(OidcConstants.HISTORICAL_KEYS_CONTENT_TYPE))
                .andReturn();
            val historicalKeys = SignedJWT.parse(result.getResponse().getContentAsString());
            assertEquals("jwk-set+jwt", historicalKeys.getHeader().getType().getType());
            assertEquals(revoked.getHeader().getKeyID(), historicalKeys.getHeader().getKeyID());
            val claims = historicalKeys.getJWTClaimsSet();
            assertEquals("https://sso.example.org/cas/oidc", claims.getIssuer());
            assertNotNull(claims.getIssueTime());
            val keys = (List<Map<String, Object>>) claims.getClaim("keys");
            assertEquals(1, keys.size());
            assertEquals(retiringKey, keys.getFirst().get("kid"));
            assertNotNull(keys.getFirst().get("exp"));
            assertNull(keys.getFirst().get("d"));
        }

        private SignedJWT fetchEntityConfiguration() throws Exception {
            val result = mockMvc.perform(get(FEDERATION_ENDPOINT_URL).with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andReturn();
            return SignedJWT.parse(result.getResponse().getContentAsString());
        }

        private static List<String> keyIds(final SignedJWT entityConfiguration) throws Exception {
            return JWKSet.parse((Map<String, Object>) entityConfiguration.getJWTClaimsSet().getClaim("jwks"))
                .getKeys().stream().map(JWK::getKeyID).toList();
        }
    }

    /**
     * CAS as a trust mark issuer (OpenID Federation 1.0, sections 7 and 8.4 to 8.6), and a trust anchor that publishes
     * the owners of delegated trust mark types (section 7.2).
     */
    @SpringBootTest(
        classes = AbstractOidcTrustAnchorFederationTests.SharedTestConfiguration.class,
        properties = {
            "cas.server.name=https://sso.example.org/",
            "cas.server.prefix=https://sso.example.org/cas",
            "cas.authn.oidc.core.issuer=https://sso.example.org/cas/oidc",
            "cas.authn.oidc.federation.role=TRUST_ANCHOR",
            "cas.authn.oidc.federation.jwks-file=file:${#systemProperties['java.io.tmpdir']}/trust-mark-issuer-federation.jwks",
            "cas.authn.oidc.federation.subordinate-directory=./src/test/resources/subordinates",
            "cas.authn.oidc.federation.issued-trust-marks[0].trust-mark-type=" + MEMBER_TRUST_MARK_TYPE,
            "cas.authn.oidc.federation.issued-trust-marks[0].subjects[0]=https://rp.example.org",
            "cas.authn.oidc.federation.issued-trust-marks[0].subjects[1]=https://op.example.org",
            "cas.authn.oidc.federation.issued-trust-marks[0].ref=https://sso.example.org/members",
            "cas.authn.oidc.federation.trust-mark-owners[0].trust-mark-type=https://refeds.org/sirtfi",
            "cas.authn.oidc.federation.trust-mark-owners[0].owner=https://refeds.org",
            "cas.authn.oidc.federation.trust-mark-owners[0].jwks=classpath:trust-marks/owner.jwks"
        }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Nested
    class TrustMarkIssuerTrustAnchorFederationTests extends AbstractOidcTrustAnchorFederationTests {
        private static final String TRUST_MARK_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + OidcConstants.TRUST_MARK_FEDERATION_URL;

        private static final String TRUST_MARK_STATUS_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + OidcConstants.TRUST_MARK_STATUS_FEDERATION_URL;

        private static final String TRUST_MARK_LIST_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + OidcConstants.TRUST_MARK_LIST_FEDERATION_URL;

        @Test
        void verifyTrustMarkEndpointsAndOwnersArePublished() throws Exception {
            val result = mockMvc.perform(get(FEDERATION_ENDPOINT_URL).with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andReturn();
            val claims = SignedJWT.parse(result.getResponse().getContentAsString()).getJWTClaimsSet();
            val federationEntity = (Map<String, Object>) ((Map<String, Object>) claims.getClaim("metadata")).get("federation_entity");
            assertEquals("https://sso.example.org/cas/oidc" + OidcConstants.TRUST_MARK_FEDERATION_URL,
                federationEntity.get("federation_trust_mark_endpoint"));
            assertEquals("https://sso.example.org/cas/oidc" + OidcConstants.TRUST_MARK_STATUS_FEDERATION_URL,
                federationEntity.get("federation_trust_mark_status_endpoint"));
            assertEquals("https://sso.example.org/cas/oidc" + OidcConstants.TRUST_MARK_LIST_FEDERATION_URL,
                federationEntity.get("federation_trust_mark_list_endpoint"));

            val owner = (Map<String, Object>) ((Map<String, Object>) claims.getClaim("trust_mark_owners")).get("https://refeds.org/sirtfi");
            assertEquals("https://refeds.org", owner.get("sub"));
            val ownerKeys = JWKSet.parse((Map<String, Object>) owner.get("jwks"));
            assertEquals("refeds-sirtfi-owner", ownerKeys.getKeys().getFirst().getKeyID());
            assertFalse(ownerKeys.getKeys().getFirst().isPrivate());
        }

        @Test
        void verifyTrustMarkIssuance() throws Exception {
            val trustMark = issueTrustMark("https://rp.example.org");
            assertEquals("trust-mark+jwt", trustMark.getHeader().getType().getType());
            assertNotNull(trustMark.getHeader().getKeyID());
            val claims = trustMark.getJWTClaimsSet();
            assertEquals("https://sso.example.org/cas/oidc", claims.getIssuer());
            assertEquals("https://rp.example.org", claims.getSubject());
            assertEquals(MEMBER_TRUST_MARK_TYPE, claims.getStringClaim("trust_mark_type"));
            assertEquals("https://sso.example.org/members", claims.getStringClaim("ref"));
            assertNotNull(claims.getIssueTime());
            assertTrue(claims.getExpirationTime().after(claims.getIssueTime()));

            mockMvc.perform(get(TRUST_MARK_URL).param("trust_mark_type", MEMBER_TRUST_MARK_TYPE).param("sub", "https://other.example.org")
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
            mockMvc.perform(get(TRUST_MARK_URL).param("trust_mark_type", "https://other.example.org/marks").param("sub", "https://rp.example.org")
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isNotFound());
            mockMvc.perform(get(TRUST_MARK_URL).param("trust_mark_type", MEMBER_TRUST_MARK_TYPE).with(withHttpRequestProcessor()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST));
        }

        @Test
        void verifyTrustMarkStatus() throws Exception {
            val trustMark = issueTrustMark("https://op.example.org");
            val result = mockMvc.perform(post(TRUST_MARK_STATUS_URL).param("trust_mark", trustMark.serialize())
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(OidcConstants.TRUST_MARK_STATUS_RESPONSE_CONTENT_TYPE))
                .andReturn();
            val response = SignedJWT.parse(result.getResponse().getContentAsString());
            assertEquals("trust-mark-status-response+jwt", response.getHeader().getType().getType());
            assertEquals(trustMark.getHeader().getKeyID(), response.getHeader().getKeyID());
            assertEquals("https://sso.example.org/cas/oidc", response.getJWTClaimsSet().getIssuer());
            assertEquals(trustMark.serialize(), response.getJWTClaimsSet().getStringClaim("trust_mark"));
            assertEquals("active", response.getJWTClaimsSet().getStringClaim("status"));

            val forgedKey = new RSAKeyGenerator(2048).keyID(trustMark.getHeader().getKeyID()).generate();
            val forged = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).type(new JOSEObjectType("trust-mark+jwt"))
                .keyID(forgedKey.getKeyID()).build(), trustMark.getJWTClaimsSet());
            forged.sign(new RSASSASigner(forgedKey));
            val forgedResult = mockMvc.perform(post(TRUST_MARK_STATUS_URL).param("trust_mark", forged.serialize())
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andReturn();
            assertEquals("invalid", SignedJWT.parse(forgedResult.getResponse().getContentAsString()).getJWTClaimsSet().getStringClaim("status"));

            val foreign = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).type(new JOSEObjectType("trust-mark+jwt")).build(),
                new JWTClaimsSet.Builder(trustMark.getJWTClaimsSet()).issuer("https://other.example.org").build());
            foreign.sign(new RSASSASigner(forgedKey));
            mockMvc.perform(post(TRUST_MARK_STATUS_URL).param("trust_mark", foreign.serialize()).with(withHttpRequestProcessor()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
            mockMvc.perform(post(TRUST_MARK_STATUS_URL).param("trust_mark", "not-a-trust-mark").with(withHttpRequestProcessor()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST));
            mockMvc.perform(post(TRUST_MARK_STATUS_URL).with(withHttpRequestProcessor()))
                .andExpect(status().isBadRequest());
        }

        @Test
        void verifyTrustMarkedEntitiesListing() throws Exception {
            mockMvc.perform(get(TRUST_MARK_LIST_URL).param("trust_mark_type", MEMBER_TRUST_MARK_TYPE).with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("https://rp.example.org"))
                .andExpect(jsonPath("$[1]").value("https://op.example.org"));
            mockMvc.perform(get(TRUST_MARK_LIST_URL).param("trust_mark_type", MEMBER_TRUST_MARK_TYPE).param("sub", "https://op.example.org")
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0]").value("https://op.example.org"));
            mockMvc.perform(get(TRUST_MARK_LIST_URL).param("trust_mark_type", "https://other.example.org/marks").with(withHttpRequestProcessor()))
                .andExpect(status().isNotFound());
            mockMvc.perform(get(TRUST_MARK_LIST_URL).with(withHttpRequestProcessor()))
                .andExpect(status().isBadRequest());
        }

        private SignedJWT issueTrustMark(final String subject) throws Exception {
            val result = mockMvc.perform(get(TRUST_MARK_URL).param("trust_mark_type", MEMBER_TRUST_MARK_TYPE).param("sub", subject)
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(OidcConstants.TRUST_MARK_CONTENT_TYPE))
                .andReturn();
            return SignedJWT.parse(result.getResponse().getContentAsString());
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
    class BadTrustAnchorRoleOpenIdProviderFederationEndpointTests extends AbstractOidcOpenIdProviderFederationTests {

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
        classes = AbstractOidcOpenIdProviderFederationTests.SharedTestConfiguration.class,
        properties = {
            "cas.server.name=https://sso.example.org/",
            "cas.server.prefix=https://sso.example.org/cas",
            "cas.authn.oidc.federation.role=INTERMEDIATE",
            "cas.authn.oidc.jwks.file-system.jwks-file=file:${#systemProperties['java.io.tmpdir']}/oidc.jwks",
            "cas.authn.oidc.federation.jwks-file=file:${#systemProperties['java.io.tmpdir']}/federation.jwks",
            "cas.authn.oidc.core.issuer=https://sso.example.org/cas/oidc"
        }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Nested
    class BadIntermediateRoleOpenIdProviderFederationEndpointTests extends AbstractOidcOpenIdProviderFederationTests {

        @Test
        void verifyOperation() throws Exception {
            val thrown = assertThrows(ServletException.class, () -> {
                mockMvc.perform(get(FEDERATION_ENDPOINT_URL)
                        .with(withHttpRequestProcessor()));
            });
            assertTrue(thrown.getCause() instanceof IllegalArgumentException);
            assertEquals("Federation role [INTERMEDIATE] is not supported for OpenID Provider", thrown.getCause().getMessage());
        }
    }

    @SpringBootTest(
        classes = AbstractOidcTrustAnchorFederationTests.SharedTestConfiguration.class,
        properties = {
            "cas.server.name=https://sso.example.org/",
            "cas.server.prefix=https://sso.example.org/cas",
            "cas.authn.oidc.federation.role=OPENID_PROVIDER",
            "cas.authn.oidc.federation.jwks-file=file:${#systemProperties['java.io.tmpdir']}/federation.jwks",
            "cas.authn.oidc.core.issuer=https://sso.example.org/cas/oidc"
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

    @SpringBootTest(
        classes = AbstractOidcOpenIdProviderFederationTests.SharedTestConfiguration.class,
        properties = {
            "cas.server.name=https://sso.example.org/",
            "cas.server.prefix=https://sso.example.org/cas",
            "cas.authn.oidc.core.issuer=https://sso.example.org/cas/oidc",
            "cas.authn.oidc.jwks.file-system.jwks-file=file:${#systemProperties['java.io.tmpdir']}/oidc.jwks",
            "cas.authn.oidc.federation.role=OPENID_PROVIDER",
            "cas.authn.oidc.federation.jwks-file=file:${#systemProperties['java.io.tmpdir']}/federation.jwks"
        }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Nested
    class MissingAuthorityHintsOpenIdProviderFederationTests extends AbstractOidcOpenIdProviderFederationTests {

        @Test
        void verifyOperation() throws Exception {
            val thrown = assertThrows(ServletException.class, () -> {
                mockMvc.perform(get(FEDERATION_ENDPOINT_URL)
                        .with(withHttpRequestProcessor()));
            });
            assertTrue(thrown.getCause() instanceof IllegalArgumentException);
            assertEquals("OpenID provider requires authority hint(s)", thrown.getCause().getMessage());
        }
    }

    @SpringBootTest(
        classes = AbstractOidcIntermediateFederationTests.SharedTestConfiguration.class,
        properties = {
            "cas.server.name=https://sso.example.org/",
            "cas.server.prefix=https://sso.example.org/cas",
            "cas.authn.oidc.core.issuer=https://sso.example.org/cas/oidc",
            "cas.authn.oidc.federation.role=INTERMEDIATE",
            "cas.authn.oidc.federation.jwks-file=file:${#systemProperties['java.io.tmpdir']}/federation.jwks",
            "cas.authn.oidc.federation.subordinate-directory=./src/test/resources/subordinates"
        }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Nested
    class MissingAuthorityHintsIntermediateFederationTests extends AbstractOidcIntermediateFederationTests {

        @Test
        void verifyOperation() throws Exception {
            val thrown = assertThrows(ServletException.class, () -> {
                mockMvc.perform(get(FEDERATION_ENDPOINT_URL)
                        .with(withHttpRequestProcessor()));
            });
            assertTrue(thrown.getCause() instanceof IllegalArgumentException);
            assertEquals("Intermediate requires authority hint(s)", thrown.getCause().getMessage());
        }
    }

    @SpringBootTest(
        classes = AbstractOidcTrustAnchorFederationTests.SharedTestConfiguration.class,
        properties = {
            "cas.server.name=https://sso.example.org/",
            "cas.server.prefix=https://sso.example.org/cas",
            "cas.authn.oidc.core.issuer=https://sso.example.org/cas/oidc",
            "cas.authn.oidc.federation.role=TRUST_ANCHOR",
            "cas.authn.oidc.federation.jwks-file=file:${#systemProperties['java.io.tmpdir']}/federation.jwks",
            "cas.authn.oidc.federation.subordinate-directory=./src/test/resources/subordinates",
            "cas.authn.oidc.federation.authority-hints[0]=https://trustanchor"
        }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Nested
    class ExistingAuthorityHintsTrustAnchorFederationTests extends AbstractOidcTrustAnchorFederationTests {

        @Test
        void verifyOperation() throws Exception {
            val thrown = assertThrows(ServletException.class, () -> {
                mockMvc.perform(get(FEDERATION_ENDPOINT_URL)
                        .with(withHttpRequestProcessor()));
            });
            assertTrue(thrown.getCause() instanceof IllegalArgumentException);
            assertEquals("Trust anchor requires no authority hints", thrown.getCause().getMessage());
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class MetadataContributorTestConfiguration {
        @Bean
        public OidcFederationEntityMetadataContributor testCredentialIssuerMetadataContributor() {
            return entityId -> Map.of(
                "openid_credential_issuer", Map.of("credential_issuer", entityId, "authorization_servers", List.of(entityId)),
                "openid_provider", Map.of("contributed", true),
                "federation_entity", Map.of("contributed", true));
        }

        @Bean
        public OidcFederationEntityMetadataContributor testAuthorizationServerMetadataContributor() {
            return entityId -> Map.of("oauth_authorization_server", Map.of("issuer", entityId));
        }
    }
}
