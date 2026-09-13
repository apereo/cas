package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.metadata.OidcCredentialIssuerMetadataService;
import org.apereo.cas.oidc.vc.issuer.nonce.OidcVerifiableCredentialNonceService;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator;
import org.apereo.cas.oidc.vc.services.DefaultRegisteredServiceOidcVerifiableCredentialsPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.dpop.DefaultDPoPProofFactory;
import com.nimbusds.oauth2.sdk.dpop.JWKThumbprintConfirmation;
import com.nimbusds.oauth2.sdk.token.DPoPAccessToken;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcVerifiableCredentialEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("OIDCWeb")
@Execution(ExecutionMode.SAME_THREAD)
class OidcVerifiableCredentialEndpointControllerTests {

    @ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
    @TestPropertySource(properties = {
        "cas.authn.attribute-repository.stub.attributes.given_name=CAS",
        "cas.authn.attribute-repository.stub.attributes.family_name=User",
        "cas.authn.attribute-repository.stub.attributes.email=casuser@example.org",
        "cas.authn.attribute-repository.stub.attributes.student_id=S12345",
        "cas.authn.attribute-repository.stub.attributes.active=true",
        "cas.authn.attribute-repository.stub.attributes.score=95.5",
        "cas.authn.attribute-repository.stub.attributes.roles=admin,user",

        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.format=DC_SD_JWT",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.scope=UniversityIDCredential",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.given_name.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.family_name.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.email.mandatory=false",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.student_id.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.active.mandatory=false",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.score.mandatory=false",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.roles.mandatory=false",

        "cas.authn.oidc.vc.issuer.credential-configurations.strict.format=DC_SD_JWT",
        "cas.authn.oidc.vc.issuer.credential-configurations.strict.scope=StrictCredential",
        "cas.authn.oidc.vc.issuer.credential-configurations.strict.claims.national_id.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.strict.claims.tax_number.mandatory=true",

        "cas.authn.oidc.vc.issuer.credential-configurations.employee.format=JWT_VC_JSON",
        "cas.authn.oidc.vc.issuer.credential-configurations.employee.scope=EmployeeCredential",
        "cas.authn.oidc.vc.issuer.credential-configurations.employee.credential-validity=P7D",
        "cas.authn.oidc.vc.issuer.credential-configurations.employee.claims.given_name.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.employee.claims.family_name.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.employee.claims.email.mandatory=false",

        "cas.authn.oidc.vc.issuer.credential-configurations.jsonld.format=JWT_VC_JSON_LD",
        "cas.authn.oidc.vc.issuer.credential-configurations.jsonld.scope=EmployeeCredential",
        "cas.authn.oidc.vc.issuer.credential-configurations.jsonld.claims.given_name.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.jsonld.claims.family_name.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.jsonld.claims.email.mandatory=false"
    })
    abstract static class BaseTests extends AbstractOidcTests {
        protected static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
            .defaultTypingEnabled(false).build().toObjectMapper();

        protected static final String CREDENTIAL_ENDPOINT_URL =
            "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_URL;

        protected static final String CREDENTIAL_ISSUER = "https://sso.example.org/cas/oidc";

        protected static final JOSEObjectType PROOF_JWT_TYPE = new JOSEObjectType("openid4vci-proof+jwt");

        @Autowired
        @Qualifier("oidcCredentialIssuerMetadataService")
        protected OidcCredentialIssuerMetadataService oidcCredentialIssuerMetadataService;

        @Autowired
        @Qualifier("oidcVerifiableCredentialProofValidator")
        protected OidcVerifiableCredentialProofValidator oidcVerifiableCredentialProofValidator;

        @Autowired
        @Qualifier(OidcVerifiableCredentialNonceService.BEAN_NAME)
        protected OidcVerifiableCredentialNonceService oidcVerifiableCredentialNonceService;

        protected static RSAKey generateRsaHolderKey() throws Exception {
            return new RSAKeyGenerator(2048).keyID("holder-rsa").generate();
        }

        protected static ECKey generateEcHolderKey() throws Exception {
            return new ECKeyGenerator(Curve.P_256).keyID("holder-ec").generate();
        }

        protected String buildProofJwt(final RSAKey holderKey, final String audience,
                                       final Date issuedAt) throws Exception {
            val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(PROOF_JWT_TYPE)
                .jwk(holderKey.toPublicJWK())
                .build();
            val nonce = oidcVerifiableCredentialNonceService.create().value();
            assertNotNull(nonce);
            assertTrue(oidcVerifiableCredentialNonceService.exists(nonce), "Nonce should exist immediately after creation");

            val claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .audience(audience)
                .subject("casuser")
                .issueTime(issuedAt)
                .claim("nonce", nonce)
                .build();
            val signedJwt = new SignedJWT(header, claims);
            signedJwt.sign(new RSASSASigner(holderKey));
            return signedJwt.serialize();
        }

        protected String buildProofJwt(final RSAKey holderKey, final String nonce) throws Exception {
            val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(PROOF_JWT_TYPE)
                .jwk(holderKey.toPublicJWK())
                .build();
            val claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .audience(CREDENTIAL_ISSUER)
                .subject("casuser")
                .issueTime(new Date())
                .claim("nonce", nonce)
                .build();
            val signedJwt = new SignedJWT(header, claims);
            signedJwt.sign(new RSASSASigner(holderKey));
            return signedJwt.serialize();
        }

        protected String buildProofJwt(final ECKey holderKey, final JWSAlgorithm algorithm,
                                       final String audience, final Date issuedAt) throws Exception {
            val header = new JWSHeader.Builder(algorithm)
                .type(PROOF_JWT_TYPE)
                .jwk(holderKey.toPublicJWK())
                .build();
            val nonce = oidcVerifiableCredentialNonceService.create().value();
            assertNotNull(nonce);
            assertTrue(oidcVerifiableCredentialNonceService.exists(nonce), "Nonce should exist immediately after creation");

            val claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .audience(audience)
                .subject("casuser")
                .issueTime(issuedAt)
                .claim("nonce", nonce)
                .build();
            val signedJwt = new SignedJWT(header, claims);
            signedJwt.sign(new ECDSASigner(holderKey));
            return signedJwt.serialize();
        }

        protected String buildValidRsaProofJwt() throws Exception {
            return buildProofJwt(generateRsaHolderKey(), CREDENTIAL_ISSUER, new Date());
        }

        protected static OidcVerifiableCredentialRequest.Proofs buildProofs(final String... jwts) {
            val proofs = new OidcVerifiableCredentialRequest.Proofs();
            proofs.setJwt(List.of(jwts));
            return proofs;
        }

        protected OAuth20AccessToken createOAuth20AccessToken(final String clientId) throws Throwable {
            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS"),
                    "family_name", List.of("User"),
                    "email", List.of("casuser@example.org"),
                    "student_id", List.of("S12345"),
                    "active", List.of("true"),
                    "score", List.of("95.5"),
                    "roles", List.of("admin", "user"),
                    "credentialConfigurationIds", List.of("myorg"))
            );
            val accessToken = getAccessToken(principal, clientId);
            when(accessToken.getGrantType()).thenReturn(OAuth20GrantTypes.PRE_AUTHORIZED_CODE);
            when(accessToken.getCredentialConfigurationIds())
                .thenReturn(List.of("myorg", "employee", "jsonld", "strict"));
            ticketRegistry.addTicket(Objects.requireNonNull(accessToken.getTicketGrantingTicket()));
            ticketRegistry.addTicket(accessToken);
            return accessToken;
        }
    }

    @Nested
    class CredentialValidityTests extends BaseTests {

        @Test
        void verifyIssuedCredentialOutlivesTheIssuanceExchange() throws Throwable {
            val claims = issueCredentialClaims("myorg");
            assertCredentialValidity(claims, Duration.ofDays(30));
        }

        @Test
        void verifyIssuedCredentialHonorsConfiguredValidity() throws Throwable {
            val claims = issueCredentialClaims("employee");
            assertCredentialValidity(claims, Duration.ofDays(7));
        }

        @Test
        void verifyJsonLdValidUntilTracksExpiration() throws Throwable {
            val claims = issueCredentialClaims("jsonld");
            assertCredentialValidity(claims, Duration.ofDays(30));
            val validUntil = Instant.parse(claims.getStringClaim("validUntil"));
            assertEquals(claims.getExpirationTime().toInstant().getEpochSecond(), validUntil.getEpochSecond());
        }

        private static void assertCredentialValidity(final JWTClaimsSet claims, final Duration expected) {
            val issuedAt = claims.getIssueTime().toInstant();
            val expiration = claims.getExpirationTime().toInstant();
            val validity = Duration.between(issuedAt, expiration);
            assertTrue(validity.minus(expected).abs().compareTo(Duration.ofSeconds(30)) <= 0,
                () -> "Credential validity was %s but %s was configured".formatted(validity, expected));
            assertTrue(expiration.isAfter(Instant.now(Clock.systemUTC()).plus(Duration.ofHours(1))),
                "Credential must remain usable long after the issuance exchange");
        }

        private JWTClaimsSet issueCredentialClaims(final String configurationId) throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId, "https://oauth\\.example\\.org.*", true, false);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId(configurationId);
            request.setProofs(buildProofs(buildValidRsaProofJwt()));

            val response = mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials[0].credential").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
            val credentials = assertInstanceOf(List.class, MAPPER.readValue(response, Map.class).get("credentials"));
            val credential = assertInstanceOf(Map.class, credentials.getFirst()).get("credential").toString();
            return SignedJWT.parse(StringUtils.substringBefore(credential, "~")).getJWTClaimsSet();
        }
    }

    @Nested
    class CredentialIssuanceTests extends BaseTests {

        @Test
        void verifyCredentialIssuanceWithJsonLd() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("jsonld");
            request.setProofs(buildProofs(buildValidRsaProofJwt()));

            val response = mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials[0].credential").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
            assertNotNull(response);
        }
        
        @Test
        void verifyCredentialIssuanceWithBearerToken() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(buildValidRsaProofJwt()));

            val response = mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials[0].credential").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
            assertNotNull(response);
        }

        @Test
        void verifyCredentialIssuanceWithDPoPToken() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(buildValidRsaProofJwt()));

            val response = mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, OAuth20Constants.TOKEN_TYPE_DPOP + ' ' + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials[0].credential").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
            assertNotNull(response);
        }

        @Test
        void verifyCredentialIssuanceRejectsUnknownAuthorizationScheme() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, OAuth20Constants.TOKEN_TYPE_BEARER));
        }

        @Test
        void verifyCredentialIssuanceWithResolvedConfigurationId() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);
            accessToken.setCredentialConfigurationIds(List.of("myorg"));
            ticketRegistry.updateTicket(accessToken);

            val request = new OidcVerifiableCredentialRequest();
            request.setProofs(buildProofs(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials[0].credential").exists());
        }

        @Test
        void verifyCredentialIssuanceWithAccessTokenParam() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .param(OAuth20Constants.ACCESS_TOKEN, accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials[0].credential").exists());
        }

        @Test
        void verifyCredentialIssuanceWithTokenParam() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .param(OAuth20Constants.TOKEN, accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials[0].credential").exists());
        }

        @Test
        void verifyCredentialIssuanceWithOptionalClaimMissing() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials[0].credential").exists());
        }

        @Test
        void verifyCredentialIssuanceWithAllClaimTypes() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials[0].credential").exists());
        }

        @Test
        void verifyCredentialIssuanceWithEcProof() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);

            val ecKey = generateEcHolderKey();
            val proofJwt = buildProofJwt(ecKey, JWSAlgorithm.ES256, CREDENTIAL_ISSUER, new Date());

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(proofJwt));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials[0].credential").exists());
        }
    }

    /**
     * The credential endpoint is an OAuth protected resource and answers in OpenID4VCI's own error
     * vocabulary, not the token endpoint's.
     */
    @Nested
    class ProtectedResourceSemanticsTests extends BaseTests {

        @Test
        void verifyMissingTokenIsChallengedRatherThanRefusedAsABadRequest() throws Throwable {
            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(MAPPER.writeValueAsString(buildRequestFor("myorg"))))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, OAuth20Constants.TOKEN_TYPE_BEARER));
        }

        @Test
        void verifyExpiredTokenIsUnauthorized() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            servicesManager.save(getOidcRegisteredService(clientId));
            val accessToken = createOAuth20AccessToken(clientId);
            ticketRegistry.deleteTicket(accessToken.getId());

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(buildRequestFor("myorg"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_TOKEN));
        }

        /**
         * The access token may be presented however the caller finds convenient; the endpoint reads it
         * from the authorization header or from a request parameter alike.
         */
        @Test
        void verifyTokenIsAcceptedAsARequestParameter() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            servicesManager.save(getOidcRegisteredService(clientId));
            val accessToken = createOAuth20AccessToken(clientId);

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .queryParam(OAuth20Constants.ACCESS_TOKEN, accessToken.getId())
                    .content(MAPPER.writeValueAsString(buildRequestFor("myorg"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials[0].credential").exists());

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .queryParam(OAuth20Constants.TOKEN, accessToken.getId())
                    .content(MAPPER.writeValueAsString(buildRequestFor("myorg"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials[0].credential").exists());
        }

        @Test
        void verifyDPoPBoundTokenIsChallengedInItsOwnScheme() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            servicesManager.save(getOidcRegisteredService(clientId));
            val accessToken = createOAuth20AccessToken(clientId);
            ticketRegistry.deleteTicket(accessToken.getId());

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, OAuth20Constants.TOKEN_TYPE_DPOP + " " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(buildRequestFor("myorg"))))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE,
                    org.hamcrest.Matchers.startsWith(OAuth20Constants.TOKEN_TYPE_DPOP + ' ')));
        }

        @Test
        void verifyUnknownCredentialTypeIsUnsupportedRatherThanDenied() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            servicesManager.save(getOidcRegisteredService(clientId));
            val accessToken = createOAuth20AccessToken(clientId);

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(buildRequestFor("NoSuchCredential"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_UNSUPPORTED_CREDENTIAL_TYPE));
        }

        @Test
        void verifyMalformedProofIsInvalidProof() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            servicesManager.save(getOidcRegisteredService(clientId));
            val accessToken = createOAuth20AccessToken(clientId);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs("not-a-valid-jwt"));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_PROOF));
        }

        @Test
        void verifyAbsentProofIsInvalidProof() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            servicesManager.save(getOidcRegisteredService(clientId));
            val accessToken = createOAuth20AccessToken(clientId);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_PROOF));
        }

        /**
         * A wallet told its nonce is stale fetches a fresh one and retries; a wallet told its proof is
         * invalid cannot. The two have to be distinguishable.
         */
        @Test
        void verifyUnknownNonceIsInvalidNonce() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            servicesManager.save(getOidcRegisteredService(clientId));
            val accessToken = createOAuth20AccessToken(clientId);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(buildProofJwt(generateRsaHolderKey(), UUID.randomUUID().toString())));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_NONCE));
        }

        @Test
        void verifySpentNonceIsInvalidNonce() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            servicesManager.save(getOidcRegisteredService(clientId));
            val accessToken = createOAuth20AccessToken(clientId);

            val nonce = oidcVerifiableCredentialNonceService.create().value();
            val holderKey = generateRsaHolderKey();

            val first = new OidcVerifiableCredentialRequest();
            first.setCredentialConfigurationId("myorg");
            first.setProofs(buildProofs(buildProofJwt(holderKey, nonce)));
            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(first)))
                .andExpect(status().isOk());

            val second = new OidcVerifiableCredentialRequest();
            second.setCredentialConfigurationId("myorg");
            second.setProofs(buildProofs(buildProofJwt(holderKey, nonce)));
            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(second)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_NONCE));
        }

        private OidcVerifiableCredentialRequest buildRequestFor(final String credentialConfigurationId) throws Exception {
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId(credentialConfigurationId);
            request.setProofs(buildProofs(buildValidRsaProofJwt()));
            return request;
        }
    }

    @Nested
    class ServiceCredentialPolicyTests extends BaseTests {

        @Test
        void verifyPolicyDeniesACredentialTypeTheTokenOtherwiseAuthorizes() throws Throwable {
            val accessToken = createAccessTokenForPolicy(Set.of("employee"));
            performCredentialRequest(accessToken, "myorg")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_CREDENTIAL_REQUEST_DENIED));
        }

        @Test
        void verifyPolicyAllowsItsOwnCredentialType() throws Throwable {
            val accessToken = createAccessTokenForPolicy(Set.of("employee"));
            performCredentialRequest(accessToken, "employee")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials[0].credential").exists());
        }

        @Test
        void verifyPolicyWithoutCredentialTypesAllowsEverything() throws Throwable {
            val accessToken = createAccessTokenForPolicy(Set.of());
            performCredentialRequest(accessToken, "myorg")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials[0].credential").exists());
        }

        /**
         * The policy is consulted when the credential is spent, not only when the token was minted, so
         * tightening a service takes effect against tokens that are already outstanding.
         */
        private OAuth20AccessToken createAccessTokenForPolicy(final Set<String> allowedCredentialTypes) throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            registeredService.setVerifiableCredentialsPolicy(
                new DefaultRegisteredServiceOidcVerifiableCredentialsPolicy(allowedCredentialTypes));
            servicesManager.save(registeredService);
            return createOAuth20AccessToken(clientId);
        }

        private ResultActions performCredentialRequest(final OAuth20AccessToken accessToken,
                                                       final String credentialConfigurationId) throws Exception {
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId(credentialConfigurationId);
            request.setProofs(buildProofs(buildValidRsaProofJwt()));
            return mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                .content(MAPPER.writeValueAsString(request)));
        }
    }

    @Nested
    class ProofOfPossessionTests extends BaseTests {
        private static final URI CREDENTIAL_ENDPOINT_URI =
            URI.create("https://sso.example.org" + CREDENTIAL_ENDPOINT_URL);

        @Test
        void verifySenderConstrainedTokenIsRejectedWithoutProof() throws Throwable {
            val holderKey = new ECKeyGenerator(Curve.P_256).keyID(UUID.randomUUID().toString()).generate();
            val accessToken = createSenderConstrainedAccessToken(holderKey);

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, OAuth20Constants.TOKEN_TYPE_DPOP + ' ' + accessToken.getId())
                    .content(MAPPER.writeValueAsString(buildCredentialRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_DPOP_PROOF));
        }

        @Test
        void verifySenderConstrainedTokenIsRejectedWhenProofIsBoundToAnotherToken() throws Throwable {
            val holderKey = new ECKeyGenerator(Curve.P_256).keyID(UUID.randomUUID().toString()).generate();
            val accessToken = createSenderConstrainedAccessToken(holderKey);

            val proofFactory = new DefaultDPoPProofFactory(holderKey, JWSAlgorithm.ES256);
            val proof = proofFactory.createDPoPJWT(HttpMethod.POST.name(), CREDENTIAL_ENDPOINT_URI,
                new DPoPAccessToken("AT-" + UUID.randomUUID()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, OAuth20Constants.TOKEN_TYPE_DPOP + ' ' + accessToken.getId())
                    .header(OAuth20Constants.DPOP, proof.serialize())
                    .content(MAPPER.writeValueAsString(buildCredentialRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_DPOP_PROOF));
        }

        @Test
        void verifySenderConstrainedTokenIsRejectedWhenProofIsSignedByAnotherKey() throws Throwable {
            val holderKey = new ECKeyGenerator(Curve.P_256).keyID(UUID.randomUUID().toString()).generate();
            val accessToken = createSenderConstrainedAccessToken(holderKey);

            val otherKey = new ECKeyGenerator(Curve.P_256).keyID(UUID.randomUUID().toString()).generate();
            val proofFactory = new DefaultDPoPProofFactory(otherKey, JWSAlgorithm.ES256);
            val proof = proofFactory.createDPoPJWT(HttpMethod.POST.name(), CREDENTIAL_ENDPOINT_URI,
                new DPoPAccessToken(accessToken.getId()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, OAuth20Constants.TOKEN_TYPE_DPOP + ' ' + accessToken.getId())
                    .header(OAuth20Constants.DPOP, proof.serialize())
                    .content(MAPPER.writeValueAsString(buildCredentialRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_DPOP_PROOF));
        }

        @Test
        void verifySenderConstrainedTokenWithValidProof() throws Throwable {
            val holderKey = new ECKeyGenerator(Curve.P_256).keyID(UUID.randomUUID().toString()).generate();
            val accessToken = createSenderConstrainedAccessToken(holderKey);

            val proofFactory = new DefaultDPoPProofFactory(holderKey, JWSAlgorithm.ES256);
            val proof = proofFactory.createDPoPJWT(HttpMethod.POST.name(), CREDENTIAL_ENDPOINT_URI,
                new DPoPAccessToken(accessToken.getId()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, OAuth20Constants.TOKEN_TYPE_DPOP + ' ' + accessToken.getId())
                    .header(OAuth20Constants.DPOP, proof.serialize())
                    .content(MAPPER.writeValueAsString(buildCredentialRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials[0].credential").exists());
        }

        @Test
        void verifyProofCannotBeReplayed() throws Throwable {
            val holderKey = new ECKeyGenerator(Curve.P_256).keyID(UUID.randomUUID().toString()).generate();
            val accessToken = createSenderConstrainedAccessToken(holderKey);

            val proofFactory = new DefaultDPoPProofFactory(holderKey, JWSAlgorithm.ES256);
            val proof = proofFactory.createDPoPJWT(HttpMethod.POST.name(), CREDENTIAL_ENDPOINT_URI,
                new DPoPAccessToken(accessToken.getId()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, OAuth20Constants.TOKEN_TYPE_DPOP + ' ' + accessToken.getId())
                    .header(OAuth20Constants.DPOP, proof.serialize())
                    .content(MAPPER.writeValueAsString(buildCredentialRequest())))
                .andExpect(status().isOk());

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, OAuth20Constants.TOKEN_TYPE_DPOP + ' ' + accessToken.getId())
                    .header(OAuth20Constants.DPOP, proof.serialize())
                    .content(MAPPER.writeValueAsString(buildCredentialRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_DPOP_PROOF));
        }

        @Test
        void verifyBearerTokenNeedsNoProof() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            servicesManager.save(getOidcRegisteredService(clientId));
            val accessToken = createOAuth20AccessToken(clientId);

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(buildCredentialRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials[0].credential").exists());
        }

        private OidcVerifiableCredentialRequest buildCredentialRequest() throws Exception {
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(buildValidRsaProofJwt()));
            return request;
        }

        /**
         * An access token is sender-constrained when the authorization server recorded a thumbprint
         * confirmation on it at issuance; that attribute, and nothing else, is what obliges the wallet
         * to prove possession of the holder key at the credential endpoint.
         */
        private OAuth20AccessToken createSenderConstrainedAccessToken(final ECKey holderKey) throws Throwable {
            val clientId = UUID.randomUUID().toString();
            servicesManager.save(getOidcRegisteredService(clientId));

            val accessToken = createOAuth20AccessToken(clientId);
            val confirmation = JWKThumbprintConfirmation.of(holderKey.toPublicJWK());
            val authentication = RegisteredServiceTestUtils.getAuthentication(
                accessToken.getAuthentication().getPrincipal(),
                Map.of(OAuth20Constants.DPOP_CONFIRMATION, List.of(confirmation.getValue().toString())));
            when(accessToken.getAuthentication()).thenReturn(authentication);
            return accessToken;
        }
    }

    @Nested
    class CredentialIssuanceJsonTests extends BaseTests {
        @Test
        void verifyCredentialIssuanceWithBearerToken() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("employee");
            request.setProofs(buildProofs(buildValidRsaProofJwt()));

            val response = mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials[0].credential").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
            assertNotNull(response);
        }
    }

    /**
     * OpenID4VCI 1.0 has no batch credential endpoint. A batch is one credential request that
     * carries several proofs, and the response carries one credential per proof, all of the same
     * credential configuration.
     */
    @Nested
    class BatchCredentialIssuanceTests extends BaseTests {
        @Test
        void verifyBatchCredentialIssuanceIsBounded() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);
            val accessToken = createOAuth20AccessToken(clientId);

            val proofJwts = new ArrayList<String>();
            for (var i = 0; i < 11; i++) {
                proofJwts.add(buildValidRsaProofJwt());
            }
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(proofJwts.toArray(String[]::new)));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST));
        }

        @Test
        void verifyOneCredentialIsIssuedPerProof() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);
            val firstProofJwt = buildProofJwt(generateRsaHolderKey(), CREDENTIAL_ISSUER, new Date());
            val secondProofJwt = buildProofJwt(generateRsaHolderKey(), CREDENTIAL_ISSUER, new Date());
            val firstNonce = SignedJWT.parse(firstProofJwt).getJWTClaimsSet().getStringClaim("nonce");
            val secondNonce = SignedJWT.parse(secondProofJwt).getJWTClaimsSet().getStringClaim("nonce");
            assertNotNull(firstNonce);
            assertNotNull(secondNonce);
            assertTrue(oidcVerifiableCredentialNonceService.exists(firstNonce));
            assertTrue(oidcVerifiableCredentialNonceService.exists(secondNonce));

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(firstProofJwt, secondProofJwt));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials.length()").value(2))
                .andExpect(jsonPath("$.credentials[0].credential").isNotEmpty())
                .andExpect(jsonPath("$.credentials[1].credential").isNotEmpty())
                .andExpect(jsonPath("$.credentials[0].format").doesNotExist())
                .andExpect(jsonPath("$.format").doesNotExist())
                .andExpect(jsonPath("$.credential").doesNotExist());

            assertFalse(oidcVerifiableCredentialNonceService.exists(firstNonce));
            assertFalse(oidcVerifiableCredentialNonceService.exists(secondNonce));
        }

        @Test
        void verifyBatchCredentialIssuanceSharesOneNonceAcrossProofs() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);
            val nonce = oidcVerifiableCredentialNonceService.create().value();
            assertNotNull(nonce);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(
                buildProofJwt(generateRsaHolderKey(), nonce),
                buildProofJwt(generateRsaHolderKey(), nonce)));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials.length()").value(2));

            assertFalse(oidcVerifiableCredentialNonceService.exists(nonce),
                "The shared nonce must be consumed once the batch has been issued");

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        void verifyBatchCredentialIssuanceWithInvalidAccessToken() throws Throwable {
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer AT-invalid-token-id")
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE,
                    org.hamcrest.Matchers.containsString("error=\"" + OAuth20Constants.INVALID_TOKEN + '"')))
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_TOKEN));
        }
    }

    /**
     * A credential identifier is only meaningful once the token response has advertised one.
     */
    @Nested
    class CredentialIdentifierTests extends BaseTests {
        @Test
        void verifyCredentialIdentifierIsRejectedWithoutAuthorizationDetails() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialIdentifier("myorg");
            request.setProofs(buildProofs(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST));
        }

        @Test
        void verifyBothCredentialIdentifiersAreRejected() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialIdentifier("myorg");
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST));
        }

        @Test
        void verifyCredentialRequestWithoutProofsIsRejected() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class CredentialIssuanceFailureTests extends BaseTests {
        @Test
        void verifyMissingAccessTokenReturnsError() throws Throwable {
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void verifyInvalidAccessTokenReturnsError() throws Throwable {
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer AT-invalid-token-id")
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void verifyMissingMandatoryClaimThrowsError() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("strict");
            request.setProofs(buildProofs(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void verifyMissingAllMandatoryClaimsThrowsError() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("strict");
            request.setProofs(buildProofs(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void verifyMissingContentTypeReturnsError() throws Throwable {
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer AT-12345")
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void verifyInvalidProofJwtReturnsError() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs("not-a-valid-jwt"));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void verifyProofWithWrongAudienceReturnsError() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);

            val proofJwt = buildProofJwt(generateRsaHolderKey(), "https://wrong-issuer.example.org", new Date());
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(proofJwt));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void verifyExpiredProofJwtReturnsError() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);

            val oldDate = Date.from(Instant.now().minus(Duration.ofMinutes(10)));
            val proofJwt = buildProofJwt(generateRsaHolderKey(), CREDENTIAL_ISSUER, oldDate);
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(proofJwt));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void verifyMissingProofReturnsError() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void verifyNullJwtInProofReturnsError() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs());

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    class JwtProofValidatorTests extends BaseTests {

        @Test
        void verifyValidRsaProof() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val proofJwt = buildProofJwt(holderKey, CREDENTIAL_ISSUER, new Date());

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(proofJwt));

            val result = oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            assertNotNull(result);
            assertEquals("jwt", result.proofType());
            assertNotNull(result.jwtId());
            assertEquals("casuser", result.subject());
            assertNotNull(result.holderJwk());
        }

        @Test
        void verifyProofWithoutExpectedTypeFails() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val nonce = oidcVerifiableCredentialNonceService.create().value();
            val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(new JOSEObjectType("JWT"))
                .jwk(holderKey.toPublicJWK())
                .build();
            val claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .audience(CREDENTIAL_ISSUER)
                .subject("casuser")
                .issueTime(new Date())
                .claim("nonce", nonce)
                .build();
            val signedJwt = new SignedJWT(header, claims);
            signedJwt.sign(new RSASSASigner(holderKey));

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(signedJwt.serialize()));
            assertThrows(IllegalArgumentException.class, () -> oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst()));
            assertTrue(oidcVerifiableCredentialNonceService.exists(nonce), "A rejected proof must not burn the nonce");
        }

        @Test
        void verifyProofCannotBeReplayed() throws Throwable {
            val proofJwt = buildValidRsaProofJwt();
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(proofJwt));

            val result = oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            assertNotNull(result);
            assertNotNull(result.nonce());
            assertFalse(oidcVerifiableCredentialNonceService.exists(result.nonce()),
                "The nonce must be consumed as part of proof validation");
            assertThrows(IllegalArgumentException.class, () -> oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst()));
        }

        @Test
        void verifyValidEcProof() throws Throwable {
            val holderKey = generateEcHolderKey();
            val proofJwt = buildProofJwt(holderKey, JWSAlgorithm.ES256, CREDENTIAL_ISSUER, new Date());

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(proofJwt));

            val result = oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            assertNotNull(result);
            assertEquals("jwt", result.proofType());
            assertNotNull(result.jwtId());
            assertEquals("casuser", result.subject());
            assertNotNull(result.holderJwk());
        }

        @Test
        void verifyInvalidSignatureFails() {
            assertThrows(Exception.class, () -> {
                val signingKey = generateRsaHolderKey();
                val differentKey = generateRsaHolderKey();
                val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(PROOF_JWT_TYPE)
                    .jwk(differentKey.toPublicJWK())
                    .build();
                val claims = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .audience(CREDENTIAL_ISSUER)
                    .subject("casuser")
                    .issueTime(new Date())
                    .build();
                val signedJwt = new SignedJWT(header, claims);
                signedJwt.sign(new RSASSASigner(signingKey));

                val request = new OidcVerifiableCredentialRequest();
                request.setCredentialConfigurationId("myorg");
                request.setProofs(buildProofs(signedJwt.serialize()));
                oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            });
        }

        @Test
        void verifyWrongAudienceFails() {
            assertThrows(IllegalArgumentException.class, () -> {
                val holderKey = generateRsaHolderKey();
                val proofJwt = buildProofJwt(holderKey, "https://wrong.example.org", new Date());

                val request = new OidcVerifiableCredentialRequest();
                request.setCredentialConfigurationId("myorg");
                request.setProofs(buildProofs(proofJwt));
                oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            });
        }

        @Test
        void verifyEmptyAudienceFails() {
            assertThrows(Exception.class, () -> {
                val holderKey = generateRsaHolderKey();
                val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(PROOF_JWT_TYPE)
                    .jwk(holderKey.toPublicJWK())
                    .build();
                val claims = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .subject("casuser")
                    .issueTime(new Date())
                    .build();
                val signedJwt = new SignedJWT(header, claims);
                signedJwt.sign(new RSASSASigner(holderKey));

                val request = new OidcVerifiableCredentialRequest();
                request.setCredentialConfigurationId("myorg");
                request.setProofs(buildProofs(signedJwt.serialize()));
                oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            });
        }

        @Test
        void verifyMissingIatFails() {
            assertThrows(IllegalArgumentException.class, () -> {
                val holderKey = generateRsaHolderKey();
                val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(PROOF_JWT_TYPE)
                    .jwk(holderKey.toPublicJWK())
                    .build();
                val claims = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .audience(CREDENTIAL_ISSUER)
                    .subject("casuser")
                    .build();
                val signedJwt = new SignedJWT(header, claims);
                signedJwt.sign(new RSASSASigner(holderKey));

                val request = new OidcVerifiableCredentialRequest();
                request.setCredentialConfigurationId("myorg");
                request.setProofs(buildProofs(signedJwt.serialize()));
                oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            });
        }

        @Test
        void verifyIatInFutureFails() {
            assertThrows(IllegalArgumentException.class, () -> {
                val holderKey = generateRsaHolderKey();
                val futureDate = Date.from(Instant.now().plus(Duration.ofMinutes(5)));
                val proofJwt = buildProofJwt(holderKey, CREDENTIAL_ISSUER, futureDate);

                val request = new OidcVerifiableCredentialRequest();
                request.setCredentialConfigurationId("myorg");
                request.setProofs(buildProofs(proofJwt));
                oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            });
        }

        @Test
        void verifyIatTooOldFails() {
            assertThrows(IllegalArgumentException.class, () -> {
                val holderKey = generateRsaHolderKey();
                val oldDate = Date.from(Instant.now().minus(Duration.ofMinutes(10)));
                val proofJwt = buildProofJwt(holderKey, CREDENTIAL_ISSUER, oldDate);

                val request = new OidcVerifiableCredentialRequest();
                request.setCredentialConfigurationId("myorg");
                request.setProofs(buildProofs(proofJwt));
                oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            });
        }

        @Test
        void verifyIatAtBoundaryOfFreshnessWindowSucceeds() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val nearBoundary = Date.from(Instant.now().minus(Duration.ofMinutes(4)));
            val proofJwt = buildProofJwt(holderKey, CREDENTIAL_ISSUER, nearBoundary);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(proofJwt));

            val result = oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            assertNotNull(result);
        }

        @Test
        void verifyEcProofWithDifferentCurve() throws Throwable {
            val holderKey = new ECKeyGenerator(Curve.P_384).keyID("holder-ec-384").generate();
            val header = new JWSHeader.Builder(JWSAlgorithm.ES384)
                .type(PROOF_JWT_TYPE)
                .jwk(holderKey.toPublicJWK())
                .build();
            var nonce = oidcVerifiableCredentialNonceService.create().value();
            val claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .audience(CREDENTIAL_ISSUER)
                .subject("casuser")
                .claim("nonce", nonce)
                .issueTime(new Date())
                .build();
            val signedJwt = new SignedJWT(header, claims);
            signedJwt.sign(new ECDSASigner(holderKey));

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(signedJwt.serialize()));

            val result = oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            assertNotNull(result);
            assertEquals("jwt", result.proofType());
        }

        @Test
        void verifyProofResultContainsCorrectJwtId() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val jwtId = UUID.randomUUID().toString();
            val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(PROOF_JWT_TYPE)
                .jwk(holderKey.toPublicJWK())
                .build();
            var nonce = oidcVerifiableCredentialNonceService.create().value();
            val claims = new JWTClaimsSet.Builder()
                .jwtID(jwtId)
                .audience(CREDENTIAL_ISSUER)
                .subject("testsubject")
                .claim("nonce", nonce)
                .issueTime(new Date())
                .build();
            val signedJwt = new SignedJWT(header, claims);
            signedJwt.sign(new RSASSASigner(holderKey));

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(signedJwt.serialize()));

            val result = oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            assertEquals(jwtId, result.jwtId());
            assertEquals("testsubject", result.subject());
        }

        @Test
        void verifyProofResultHolderJwkMatchesPublicKey() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val proofJwt = buildProofJwt(holderKey, CREDENTIAL_ISSUER, new Date());

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(proofJwt));

            val result = oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            assertNotNull(result.holderJwk());
            assertEquals(holderKey.toPublicJWK().toJSONString(), result.holderJwk().toJSONString());
        }

        @Test
        void verifyMalformedJwtStringFails() {
            assertThrows(Exception.class, () -> {
                val request = new OidcVerifiableCredentialRequest();
                request.setCredentialConfigurationId("myorg");
                request.setProofs(buildProofs("this.is.not.a.jwt"));
                oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            });
        }

        @Test
        void verifyCompletelyInvalidJwtFails() {
            assertThrows(Exception.class, () -> {
                val request = new OidcVerifiableCredentialRequest();
                request.setCredentialConfigurationId("myorg");
                request.setProofs(buildProofs("garbage-data"));
                oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            });
        }

        @Test
        void verifyIatSlightlyInFutureWithinToleranceSucceeds() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val slightlyFuture = Date.from(Instant.now().plus(Duration.ofSeconds(10)));
            val proofJwt = buildProofJwt(holderKey, CREDENTIAL_ISSUER, slightlyFuture);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(proofJwt));

            val result = oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            assertNotNull(result);
        }

        @Test
        void verifyIatExactlyAtFutureBoundaryFails() {
            assertThrows(IllegalArgumentException.class, () -> {
                val holderKey = generateRsaHolderKey();
                val futureDate = Date.from(Instant.now().plus(Duration.ofSeconds(60)));
                val proofJwt = buildProofJwt(holderKey, CREDENTIAL_ISSUER, futureDate);

                val request = new OidcVerifiableCredentialRequest();
                request.setCredentialConfigurationId("myorg");
                request.setProofs(buildProofs(proofJwt));
                oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            });
        }

        @Test
        void verifyMultipleAudiencesWithCorrectOneSucceeds() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(PROOF_JWT_TYPE)
                .jwk(holderKey.toPublicJWK())
                .build();
            var nonce = oidcVerifiableCredentialNonceService.create().value();
            val claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .audience(List.of("https://other.example.org", CREDENTIAL_ISSUER))
                .subject("casuser")
                .issueTime(new Date())
                .claim("nonce", nonce)
                .build();
            val signedJwt = new SignedJWT(header, claims);
            signedJwt.sign(new RSASSASigner(holderKey));

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(signedJwt.serialize()));

            val result = oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            assertNotNull(result);
        }

        @Test
        void verifyMultipleAudiencesWithoutCorrectOneFails() {
            assertThrows(IllegalArgumentException.class, () -> {
                val holderKey = generateRsaHolderKey();
                val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(PROOF_JWT_TYPE)
                    .jwk(holderKey.toPublicJWK())
                    .build();
                val claims = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .audience(List.of("https://other.example.org", "https://another.example.org"))
                    .subject("casuser")
                    .issueTime(new Date())
                    .build();
                val signedJwt = new SignedJWT(header, claims);
                signedJwt.sign(new RSASSASigner(holderKey));

                val request = new OidcVerifiableCredentialRequest();
                request.setCredentialConfigurationId("myorg");
                request.setProofs(buildProofs(signedJwt.serialize()));
                oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            });
        }

        @Test
        void verifyNullProofJwtFails() {
            assertThrows(Exception.class, () -> {
                val request = new OidcVerifiableCredentialRequest();
                request.setCredentialConfigurationId("myorg");
                request.setProofs(buildProofs());
                oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            });
        }

        @Test
        void verifyEmptyStringJwtFails() {
            assertThrows(Exception.class, () -> {
                val request = new OidcVerifiableCredentialRequest();
                request.setCredentialConfigurationId("myorg");
                request.setProofs(buildProofs(StringUtils.EMPTY));
                oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            });
        }

        @Test
        void verifyProofWithNoHeaderJwkFails() {
            assertThrows(Exception.class, () -> {
                val holderKey = generateRsaHolderKey();
                val header = new JWSHeader.Builder(JWSAlgorithm.RS256).build();
                val claims = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .audience(CREDENTIAL_ISSUER)
                    .subject("casuser")
                    .issueTime(new Date())
                    .build();
                val signedJwt = new SignedJWT(header, claims);
                signedJwt.sign(new RSASSASigner(holderKey));

                val request = new OidcVerifiableCredentialRequest();
                request.setCredentialConfigurationId("myorg");
                request.setProofs(buildProofs(signedJwt.serialize()));
                oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            });
        }

        @Test
        void verifyProofWithNoSubjectSucceeds() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(PROOF_JWT_TYPE)
                .jwk(holderKey.toPublicJWK())
                .build();
            var nonce = oidcVerifiableCredentialNonceService.create().value();
            val claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .audience(CREDENTIAL_ISSUER)
                .issueTime(new Date())
                .claim("nonce", nonce)
                .build();
            val signedJwt = new SignedJWT(header, claims);
            signedJwt.sign(new RSASSASigner(holderKey));

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(signedJwt.serialize()));

            val result = oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            assertNotNull(result);
            assertNull(result.subject());
        }

        @Test
        void verifyProofWithNoJwtIdSucceeds() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(PROOF_JWT_TYPE)
                .jwk(holderKey.toPublicJWK())
                .build();
            var nonce = oidcVerifiableCredentialNonceService.create().value();
            val claims = new JWTClaimsSet.Builder()
                .audience(CREDENTIAL_ISSUER)
                .subject("casuser")
                .claim("nonce", nonce)
                .issueTime(new Date())
                .build();
            val signedJwt = new SignedJWT(header, claims);
            signedJwt.sign(new RSASSASigner(holderKey));

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(signedJwt.serialize()));

            val result = oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            assertNotNull(result);
            assertNull(result.jwtId());
        }

        @Test
        void verifyEcKeyWithRsaAlgorithmFails() {
            assertThrows(Exception.class, () -> {
                val ecKey = generateEcHolderKey();
                val rsaKey = generateRsaHolderKey();
                val header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                    .type(PROOF_JWT_TYPE)
                    .jwk(rsaKey.toPublicJWK())
                    .build();
                val claims = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .audience(CREDENTIAL_ISSUER)
                    .subject("casuser")
                    .issueTime(new Date())
                    .build();
                val signedJwt = new SignedJWT(header, claims);
                signedJwt.sign(new ECDSASigner(ecKey));

                val request = new OidcVerifiableCredentialRequest();
                request.setCredentialConfigurationId("myorg");
                request.setProofs(buildProofs(signedJwt.serialize()));
                oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            });
        }

        @Test
        void verifyRsaProofWithRS384Algorithm() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val header = new JWSHeader.Builder(JWSAlgorithm.RS384)
                .type(PROOF_JWT_TYPE)
                .jwk(holderKey.toPublicJWK())
                .build();
            val claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .audience(CREDENTIAL_ISSUER)
                .subject("casuser")
                .issueTime(new Date())
                .claim("nonce", oidcVerifiableCredentialNonceService.create().value())
                .build();
            val signedJwt = new SignedJWT(header, claims);
            signedJwt.sign(new RSASSASigner(holderKey));

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(signedJwt.serialize()));

            val result = oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            assertNotNull(result);
            assertEquals("jwt", result.proofType());
        }

        @Test
        void verifyRsaProofWithRS512Algorithm() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val header = new JWSHeader.Builder(JWSAlgorithm.RS512)
                .type(PROOF_JWT_TYPE)
                .jwk(holderKey.toPublicJWK())
                .build();
            val claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .audience(CREDENTIAL_ISSUER)
                .subject("casuser")
                .issueTime(new Date())
                .claim("nonce", oidcVerifiableCredentialNonceService.create().value())
                .build();
            val signedJwt = new SignedJWT(header, claims);
            signedJwt.sign(new RSASSASigner(holderKey));

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(signedJwt.serialize()));

            val result = oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            assertNotNull(result);
            assertEquals("jwt", result.proofType());
        }

        @Test
        void verifyEcProofWithES512Algorithm() throws Throwable {
            val holderKey = new ECKeyGenerator(Curve.P_521).keyID("holder-ec-521").generate();
            val header = new JWSHeader.Builder(JWSAlgorithm.ES512)
                .type(PROOF_JWT_TYPE)
                .jwk(holderKey.toPublicJWK())
                .build();
            var nonce = oidcVerifiableCredentialNonceService.create().value();
            val claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .audience(CREDENTIAL_ISSUER)
                .subject("casuser")
                .claim("nonce", nonce)
                .issueTime(new Date())
                .build();
            val signedJwt = new SignedJWT(header, claims);
            signedJwt.sign(new ECDSASigner(holderKey));

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(signedJwt.serialize()));

            val result = oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            assertNotNull(result);
            assertEquals("jwt", result.proofType());
        }

        @Test
        void verifySingleAudienceExactMatchSucceeds() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val proofJwt = buildProofJwt(holderKey, CREDENTIAL_ISSUER, new Date());

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProofs(buildProofs(proofJwt));

            val result = oidcVerifiableCredentialProofValidator.validate(request.getProofs().getJwt().getFirst());
            assertNotNull(result);
            assertEquals("casuser", result.subject());
        }
    }

    @Nested
    class MetadataServiceTests extends BaseTests {

        @Test
        void verifyMetadataBuild() {
            val metadata = oidcCredentialIssuerMetadataService.build();
            assertNotNull(metadata);
            assertEquals(casProperties.getAuthn().getOidc().getCore().getIssuer(), metadata.getCredentialIssuer());
            assertNotNull(metadata.getAuthorizationServers());
            assertFalse(metadata.getAuthorizationServers().isEmpty());
            assertEquals(1, metadata.getAuthorizationServers().size());
            assertEquals(casProperties.getAuthn().getOidc().getCore().getIssuer(), metadata.getAuthorizationServers().getFirst());
        }

        @Test
        void verifyMetadataCredentialEndpoint() {
            val metadata = oidcCredentialIssuerMetadataService.build();
            val expectedEndpoint = casProperties.getAuthn().getOidc().getCore().getIssuer()
                + '/' + OidcConstants.VC_CREDENTIAL_URL;
            assertEquals(expectedEndpoint, metadata.getCredentialEndpoint());
        }

        @Test
        void verifyMetadataCredentialConfigurationsSupported() {
            val metadata = oidcCredentialIssuerMetadataService.build();
            assertNotNull(metadata.getCredentialConfigurationsSupported());
            assertFalse(metadata.getCredentialConfigurationsSupported().isEmpty());
            assertTrue(metadata.getCredentialConfigurationsSupported().containsKey("myorg"));
            val cfg = metadata.getCredentialConfigurationsSupported().get("myorg");
            assertEquals(CredentialConfigurationFormats.DC_SD_JWT.getValue(), cfg.getFormat());
            assertEquals("UniversityIDCredential", cfg.getScope());
        }

        @Test
        void verifyMetadataClaimsConfiguration() {
            val metadata = oidcCredentialIssuerMetadataService.build();
            val cfg = metadata.getCredentialConfigurationsSupported().get("myorg");
            assertNotNull(cfg.getCredentialMetadata().getClaims());
            assertEquals(7, cfg.getCredentialMetadata().getClaims().size());
        }

        @Test
        void verifyMetadataProofTypesSupported() {
            val metadata = oidcCredentialIssuerMetadataService.build();
            val cfg = metadata.getCredentialConfigurationsSupported().get("myorg");
            assertNotNull(cfg.getProofTypesSupported());
            assertFalse(cfg.getProofTypesSupported().isEmpty());
            assertTrue(cfg.getProofTypesSupported().containsKey("jwt"));
            val proof = cfg.getProofTypesSupported().get("jwt");
            assertNotNull(proof.getProofSigningAlgValuesSupported());
            assertFalse(proof.getProofSigningAlgValuesSupported().isEmpty());
        }

        @Test
        void verifyMetadataCryptoBindingAndSigningAlgs() {
            val metadata = oidcCredentialIssuerMetadataService.build();
            val cfg = metadata.getCredentialConfigurationsSupported().get("myorg");
            assertNotNull(cfg.getCryptographicBindingMethodsSupported());
            assertFalse(cfg.getCryptographicBindingMethodsSupported().isEmpty());
            assertNotNull(cfg.getCredentialSigningAlgValuesSupported());
            assertFalse(cfg.getCredentialSigningAlgValuesSupported().isEmpty());
        }

        @Test
        void verifyMetadataDefaultValues() {
            val metadata = oidcCredentialIssuerMetadataService.build();
            val cfg = metadata.getCredentialConfigurationsSupported().get("myorg");
            assertTrue(cfg.getCryptographicBindingMethodsSupported().contains("jwk"));
            assertTrue(cfg.getCredentialSigningAlgValuesSupported().contains("ES256"));
            assertTrue(cfg.getCredentialSigningAlgValuesSupported().contains("RS256"));

            val proof = cfg.getProofTypesSupported().get("jwt");
            assertTrue(proof.getProofSigningAlgValuesSupported().contains("ES256"));
            assertTrue(proof.getProofSigningAlgValuesSupported().contains("RS256"));
        }
    }
}
