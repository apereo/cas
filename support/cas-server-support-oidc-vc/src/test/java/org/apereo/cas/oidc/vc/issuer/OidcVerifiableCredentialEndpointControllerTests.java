package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.metadata.CredentialConfigurationFormats;
import org.apereo.cas.oidc.vc.issuer.metadata.OidcCredentialIssuerMetadataService;
import org.apereo.cas.oidc.vc.issuer.nonce.OidcVerifiableCredentialNonceService;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator;
import org.apereo.cas.oidc.vc.issuer.web.OidcVerifiableCredentialEndpointController.OidcVcBatchCredentialRequest;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
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
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
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

        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.format=dc+sd-jwt",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.scope=UniversityIDCredential",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.given_name.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.family_name.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.email.mandatory=false",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.student_id.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.active.mandatory=false",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.score.mandatory=false",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.roles.mandatory=false",

        "cas.authn.oidc.vc.issuer.credential-configurations.strict.format=dc+sd-jwt",
        "cas.authn.oidc.vc.issuer.credential-configurations.strict.scope=StrictCredential",
        "cas.authn.oidc.vc.issuer.credential-configurations.strict.claims.national_id.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.strict.claims.tax_number.mandatory=true",

        "cas.authn.oidc.vc.issuer.credential-configurations.employee.format=jwt_vc_json",
        "cas.authn.oidc.vc.issuer.credential-configurations.employee.scope=EmployeeCredential",
        "cas.authn.oidc.vc.issuer.credential-configurations.employee.claims.given_name.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.employee.claims.family_name.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.employee.claims.email.mandatory=false",

        "cas.authn.oidc.vc.issuer.credential-configurations.jsonld.format=jwt_vc_json-ld",
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

        protected static final String BATCH_CREDENTIAL_ENDPOINT_URL =
            "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_BATCH_CREDENTIAL_URL;

        protected static final String CREDENTIAL_ISSUER = "https://sso.example.org/cas/oidc";

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

        protected String buildProofJwt(final ECKey holderKey, final JWSAlgorithm algorithm,
                                       final String audience, final Date issuedAt) throws Exception {
            val header = new JWSHeader.Builder(algorithm)
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

        protected static OidcVerifiableCredentialRequest.Proof buildProof(final String jwt) {
            val proof = new OidcVerifiableCredentialRequest.Proof();
            proof.setProofType("jwt");
            proof.setJwt(jwt);
            return proof;
        }

        protected OAuth20AccessToken createOAuth20AccessToken(final String clientId) throws Throwable {
            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS"),
                    "family_name", List.of("User"),
                    "email", List.of("casuser@example.org"),
                    "student_id", List.of("S12345"),
                    "active", List.of("true"),
                    "score", List.of("95.5"),
                    "roles", List.of("admin", "user"))
            );
            val accessToken = getAccessToken(principal, clientId);
            when(accessToken.getGrantType()).thenReturn(OAuth20GrantTypes.PRE_AUTHORIZED_CODE);
            ticketRegistry.addTicket(Objects.requireNonNull(accessToken.getTicketGrantingTicket()));
            ticketRegistry.addTicket(accessToken);
            return accessToken;
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
            request.setProof(buildProof(buildValidRsaProofJwt()));

            val response = mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value(CredentialConfigurationFormats.JWT_VC_JSON_LD.getFormat()))
                .andExpect(jsonPath("$.credential").exists())
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
            request.setProof(buildProof(buildValidRsaProofJwt()));

            val response = mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value(CredentialConfigurationFormats.DC_SD_JWT.getFormat()))
                .andExpect(jsonPath("$.credential").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
            assertNotNull(response);
        }

        @Test
        void verifyCredentialIssuanceWithAccessTokenParam() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProof(buildProof(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .param(OAuth20Constants.ACCESS_TOKEN, accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value(CredentialConfigurationFormats.DC_SD_JWT.getFormat()))
                .andExpect(jsonPath("$.credential").exists());
        }

        @Test
        void verifyCredentialIssuanceWithTokenParam() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProof(buildProof(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .param(OAuth20Constants.TOKEN, accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value(CredentialConfigurationFormats.DC_SD_JWT.getFormat()))
                .andExpect(jsonPath("$.credential").exists());
        }

        @Test
        void verifyCredentialIssuanceWithOptionalClaimMissing() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProof(buildProof(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credential").exists());
        }

        @Test
        void verifyCredentialIssuanceWithAllClaimTypes() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProof(buildProof(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value(CredentialConfigurationFormats.DC_SD_JWT.getFormat()))
                .andExpect(jsonPath("$.credential").exists());
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
            request.setProof(buildProof(proofJwt));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value(CredentialConfigurationFormats.DC_SD_JWT.getFormat()))
                .andExpect(jsonPath("$.credential").exists());
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
            request.setProof(buildProof(buildValidRsaProofJwt()));

            val response = mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value(CredentialConfigurationFormats.JWT_VC_JSON.getFormat()))
                .andExpect(jsonPath("$.credential").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
            assertNotNull(response);
        }
    }

    @Nested
    class BatchCredentialIssuanceTests extends BaseTests {
        @ParameterizedTest
        @ValueSource(strings = {BATCH_CREDENTIAL_ENDPOINT_URL, CREDENTIAL_ENDPOINT_URL})
        void verifyBatchCredentialIssuance(final String endpointUrl) throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val accessToken = createOAuth20AccessToken(clientId);
            val holderKey = generateRsaHolderKey();
            val firstProofJwt = buildProofJwt(holderKey, CREDENTIAL_ISSUER, new Date());
            val secondProofJwt = buildProofJwt(holderKey, CREDENTIAL_ISSUER, new Date());
            val firstNonce = SignedJWT.parse(firstProofJwt).getJWTClaimsSet().getStringClaim("nonce");
            val secondNonce = SignedJWT.parse(secondProofJwt).getJWTClaimsSet().getStringClaim("nonce");
            assertNotNull(firstNonce);
            assertNotNull(secondNonce);
            assertTrue(oidcVerifiableCredentialNonceService.exists(firstNonce));
            assertTrue(oidcVerifiableCredentialNonceService.exists(secondNonce));

            val firstRequest = new OidcVerifiableCredentialRequest();
            firstRequest.setCredentialConfigurationId("myorg");
            firstRequest.setProof(buildProof(firstProofJwt));

            val secondRequest = new OidcVerifiableCredentialRequest();
            secondRequest.setCredentialConfigurationId("employee");
            secondRequest.setProof(buildProof(secondProofJwt));
            val batchRequest = new OidcVcBatchCredentialRequest(List.of(firstRequest, secondRequest));

            mockMvc.perform(post(endpointUrl)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(batchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credential_responses.length()").value(2))
                .andExpect(jsonPath("$.credential_responses[0].format")
                    .value(CredentialConfigurationFormats.DC_SD_JWT.getFormat()))
                .andExpect(jsonPath("$.credential_responses[0].credential").isNotEmpty())
                .andExpect(jsonPath("$.credential_responses[1].format")
                    .value(CredentialConfigurationFormats.JWT_VC_JSON.getFormat()))
                .andExpect(jsonPath("$.credential_responses[1].credential").isNotEmpty());

            assertFalse(oidcVerifiableCredentialNonceService.exists(firstNonce));
            assertFalse(oidcVerifiableCredentialNonceService.exists(secondNonce));
        }

        @Test
        void verifyBatchCredentialIssuanceWithInvalidAccessToken() throws Throwable {
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            val batchRequest = new OidcVcBatchCredentialRequest(List.of(request));

            mockMvc.perform(post(BATCH_CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer AT-invalid-token-id")
                    .content(MAPPER.writeValueAsString(batchRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST));
        }
    }

    @Nested
    class CredentialIssuanceFailureTests extends BaseTests {
        @Test
        void verifyMissingAccessTokenReturnsError() throws Throwable {
            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProof(buildProof(buildValidRsaProofJwt()));

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
            request.setProof(buildProof(buildValidRsaProofJwt()));

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
            request.setProof(buildProof(buildValidRsaProofJwt()));

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
            request.setProof(buildProof(buildValidRsaProofJwt()));

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
            request.setProof(buildProof(buildValidRsaProofJwt()));

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
            request.setProof(buildProof("not-a-valid-jwt"));

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
            request.setProof(buildProof(proofJwt));

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
            request.setProof(buildProof(proofJwt));

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
            request.setProof(buildProof(null));

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
            request.setProof(buildProof(proofJwt));

            val result = oidcVerifiableCredentialProofValidator.validate(request);
            assertNotNull(result);
            assertEquals("jwt", result.proofType());
            assertNotNull(result.jwtId());
            assertEquals("casuser", result.subject());
            assertNotNull(result.holderJwk());
        }

        @Test
        void verifyValidEcProof() throws Throwable {
            val holderKey = generateEcHolderKey();
            val proofJwt = buildProofJwt(holderKey, JWSAlgorithm.ES256, CREDENTIAL_ISSUER, new Date());

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProof(buildProof(proofJwt));

            val result = oidcVerifiableCredentialProofValidator.validate(request);
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
                request.setProof(buildProof(signedJwt.serialize()));
                oidcVerifiableCredentialProofValidator.validate(request);
            });
        }

        @Test
        void verifyWrongAudienceFails() {
            assertThrows(IllegalArgumentException.class, () -> {
                val holderKey = generateRsaHolderKey();
                val proofJwt = buildProofJwt(holderKey, "https://wrong.example.org", new Date());

                val request = new OidcVerifiableCredentialRequest();
                request.setCredentialConfigurationId("myorg");
                request.setProof(buildProof(proofJwt));
                oidcVerifiableCredentialProofValidator.validate(request);
            });
        }

        @Test
        void verifyEmptyAudienceFails() {
            assertThrows(Exception.class, () -> {
                val holderKey = generateRsaHolderKey();
                val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
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
                request.setProof(buildProof(signedJwt.serialize()));
                oidcVerifiableCredentialProofValidator.validate(request);
            });
        }

        @Test
        void verifyMissingIatFails() {
            assertThrows(IllegalArgumentException.class, () -> {
                val holderKey = generateRsaHolderKey();
                val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
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
                request.setProof(buildProof(signedJwt.serialize()));
                oidcVerifiableCredentialProofValidator.validate(request);
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
                request.setProof(buildProof(proofJwt));
                oidcVerifiableCredentialProofValidator.validate(request);
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
                request.setProof(buildProof(proofJwt));
                oidcVerifiableCredentialProofValidator.validate(request);
            });
        }

        @Test
        void verifyIatAtBoundaryOfFreshnessWindowSucceeds() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val nearBoundary = Date.from(Instant.now().minus(Duration.ofMinutes(4)));
            val proofJwt = buildProofJwt(holderKey, CREDENTIAL_ISSUER, nearBoundary);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProof(buildProof(proofJwt));

            val result = oidcVerifiableCredentialProofValidator.validate(request);
            assertNotNull(result);
        }

        @Test
        void verifyEcProofWithDifferentCurve() throws Throwable {
            val holderKey = new ECKeyGenerator(Curve.P_384).keyID("holder-ec-384").generate();
            val header = new JWSHeader.Builder(JWSAlgorithm.ES384)
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
            request.setProof(buildProof(signedJwt.serialize()));

            val result = oidcVerifiableCredentialProofValidator.validate(request);
            assertNotNull(result);
            assertEquals("jwt", result.proofType());
        }

        @Test
        void verifyProofResultContainsCorrectJwtId() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val jwtId = UUID.randomUUID().toString();
            val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
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
            request.setProof(buildProof(signedJwt.serialize()));

            val result = oidcVerifiableCredentialProofValidator.validate(request);
            assertEquals(jwtId, result.jwtId());
            assertEquals("testsubject", result.subject());
        }

        @Test
        void verifyProofResultHolderJwkMatchesPublicKey() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val proofJwt = buildProofJwt(holderKey, CREDENTIAL_ISSUER, new Date());

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProof(buildProof(proofJwt));

            val result = oidcVerifiableCredentialProofValidator.validate(request);
            assertNotNull(result.holderJwk());
            assertEquals(holderKey.toPublicJWK().toJSONString(), result.holderJwk().toJSONString());
        }

        @Test
        void verifyMalformedJwtStringFails() {
            assertThrows(Exception.class, () -> {
                val request = new OidcVerifiableCredentialRequest();
                request.setCredentialConfigurationId("myorg");
                request.setProof(buildProof("this.is.not.a.jwt"));
                oidcVerifiableCredentialProofValidator.validate(request);
            });
        }

        @Test
        void verifyCompletelyInvalidJwtFails() {
            assertThrows(Exception.class, () -> {
                val request = new OidcVerifiableCredentialRequest();
                request.setCredentialConfigurationId("myorg");
                request.setProof(buildProof("garbage-data"));
                oidcVerifiableCredentialProofValidator.validate(request);
            });
        }

        @Test
        void verifyIatSlightlyInFutureWithinToleranceSucceeds() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val slightlyFuture = Date.from(Instant.now().plus(Duration.ofSeconds(10)));
            val proofJwt = buildProofJwt(holderKey, CREDENTIAL_ISSUER, slightlyFuture);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProof(buildProof(proofJwt));

            val result = oidcVerifiableCredentialProofValidator.validate(request);
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
                request.setProof(buildProof(proofJwt));
                oidcVerifiableCredentialProofValidator.validate(request);
            });
        }

        @Test
        void verifyMultipleAudiencesWithCorrectOneSucceeds() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
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
            request.setProof(buildProof(signedJwt.serialize()));

            val result = oidcVerifiableCredentialProofValidator.validate(request);
            assertNotNull(result);
        }

        @Test
        void verifyMultipleAudiencesWithoutCorrectOneFails() {
            assertThrows(IllegalArgumentException.class, () -> {
                val holderKey = generateRsaHolderKey();
                val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
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
                request.setProof(buildProof(signedJwt.serialize()));
                oidcVerifiableCredentialProofValidator.validate(request);
            });
        }

        @Test
        void verifyNullProofJwtFails() {
            assertThrows(Exception.class, () -> {
                val request = new OidcVerifiableCredentialRequest();
                request.setCredentialConfigurationId("myorg");
                request.setProof(buildProof(null));
                oidcVerifiableCredentialProofValidator.validate(request);
            });
        }

        @Test
        void verifyEmptyStringJwtFails() {
            assertThrows(Exception.class, () -> {
                val request = new OidcVerifiableCredentialRequest();
                request.setCredentialConfigurationId("myorg");
                request.setProof(buildProof(StringUtils.EMPTY));
                oidcVerifiableCredentialProofValidator.validate(request);
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
                request.setProof(buildProof(signedJwt.serialize()));
                oidcVerifiableCredentialProofValidator.validate(request);
            });
        }

        @Test
        void verifyProofWithNoSubjectSucceeds() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
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
            request.setProof(buildProof(signedJwt.serialize()));

            val result = oidcVerifiableCredentialProofValidator.validate(request);
            assertNotNull(result);
            assertNull(result.subject());
        }

        @Test
        void verifyProofWithNoJwtIdSucceeds() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
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
            request.setProof(buildProof(signedJwt.serialize()));

            val result = oidcVerifiableCredentialProofValidator.validate(request);
            assertNotNull(result);
            assertNull(result.jwtId());
        }

        @Test
        void verifyEcKeyWithRsaAlgorithmFails() {
            assertThrows(Exception.class, () -> {
                val ecKey = generateEcHolderKey();
                val rsaKey = generateRsaHolderKey();
                val header = new JWSHeader.Builder(JWSAlgorithm.ES256)
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
                request.setProof(buildProof(signedJwt.serialize()));
                oidcVerifiableCredentialProofValidator.validate(request);
            });
        }

        @Test
        void verifyRsaProofWithRS384Algorithm() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val header = new JWSHeader.Builder(JWSAlgorithm.RS384)
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
            request.setProof(buildProof(signedJwt.serialize()));

            val result = oidcVerifiableCredentialProofValidator.validate(request);
            assertNotNull(result);
            assertEquals("jwt", result.proofType());
        }

        @Test
        void verifyRsaProofWithRS512Algorithm() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val header = new JWSHeader.Builder(JWSAlgorithm.RS512)
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
            request.setProof(buildProof(signedJwt.serialize()));

            val result = oidcVerifiableCredentialProofValidator.validate(request);
            assertNotNull(result);
            assertEquals("jwt", result.proofType());
        }

        @Test
        void verifyEcProofWithES512Algorithm() throws Throwable {
            val holderKey = new ECKeyGenerator(Curve.P_521).keyID("holder-ec-521").generate();
            val header = new JWSHeader.Builder(JWSAlgorithm.ES512)
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
            request.setProof(buildProof(signedJwt.serialize()));

            val result = oidcVerifiableCredentialProofValidator.validate(request);
            assertNotNull(result);
            assertEquals("jwt", result.proofType());
        }

        @Test
        void verifySingleAudienceExactMatchSucceeds() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val proofJwt = buildProofJwt(holderKey, CREDENTIAL_ISSUER, new Date());

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProof(buildProof(proofJwt));

            val result = oidcVerifiableCredentialProofValidator.validate(request);
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
            assertEquals(CredentialConfigurationFormats.DC_SD_JWT.getFormat(), cfg.getFormat());
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
