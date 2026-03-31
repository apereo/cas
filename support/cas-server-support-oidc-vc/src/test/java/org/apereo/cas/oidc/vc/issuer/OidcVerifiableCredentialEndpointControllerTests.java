package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.metadata.OidcCredentialIssuerMetadataService;
import org.apereo.cas.oidc.vc.issuer.nonce.OidcVerifiableCredentialNonceService;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcVerifiableCredentialEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("OIDCWeb")
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

        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.format=vc+sd-jwt",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.scope=UniversityIDCredential",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.given_name.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.given_name.value-type=string",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.family_name.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.family_name.value-type=string",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.email.mandatory=false",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.email.value-type=string",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.student_id.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.student_id.value-type=string",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.active.mandatory=false",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.active.value-type=boolean",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.score.mandatory=false",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.score.value-type=number",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.roles.mandatory=false",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.roles.value-type=array",

        "cas.authn.oidc.vc.issuer.credential-configurations.strict.format=vc+sd-jwt",
        "cas.authn.oidc.vc.issuer.credential-configurations.strict.scope=StrictCredential",
        "cas.authn.oidc.vc.issuer.credential-configurations.strict.claims.national_id.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.strict.claims.national_id.value-type=string",
        "cas.authn.oidc.vc.issuer.credential-configurations.strict.claims.tax_number.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.strict.claims.tax_number.value-type=string"
    })
    abstract static class BaseTests extends AbstractOidcTests {
        protected static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
            .defaultTypingEnabled(false).build().toObjectMapper();

        protected static final String CREDENTIAL_ENDPOINT_URL =
            "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_URL;

        protected static final String CREDENTIAL_ISSUER = "https://sso.example.org/cas/oidc";

        @Autowired
        @Qualifier("oidcCredentialIssuerMetadataService")
        protected OidcCredentialIssuerMetadataService oidcCredentialIssuerMetadataService;

        @Autowired
        @Qualifier("oidcVerifiableCredentialProofValidator")
        protected OidcVerifiableCredentialProofValidator oidcVerifiableCredentialProofValidator;

        @Autowired
        @Qualifier("oidcVerifiableCredentialNonceService")
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
            val claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .audience(audience)
                .subject("casuser")
                .issueTime(issuedAt)
                .claim("nonce", oidcVerifiableCredentialNonceService.create().value())
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
            val claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .audience(audience)
                .subject("casuser")
                .issueTime(issuedAt)
                .claim("nonce", oidcVerifiableCredentialNonceService.create().value())
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
    }

    @Nested
    class CredentialIssuanceTests extends BaseTests {

        @Test
        void verifyCredentialIssuanceWithBearerToken() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS"),
                    "family_name", List.of("User"),
                    "email", List.of("casuser@example.org"),
                    "student_id", List.of("S12345")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProof(buildProof(buildValidRsaProofJwt()));

            val response = mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value("vc+sd-jwt"))
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

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS"),
                    "family_name", List.of("User"),
                    "email", List.of("casuser@example.org"),
                    "student_id", List.of("S12345")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProof(buildProof(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .param(OAuth20Constants.ACCESS_TOKEN, accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value("vc+sd-jwt"))
                .andExpect(jsonPath("$.credential").exists());
        }

        @Test
        void verifyCredentialIssuanceWithTokenParam() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS"),
                    "family_name", List.of("User"),
                    "email", List.of("casuser@example.org"),
                    "student_id", List.of("S12345")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProof(buildProof(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .param(OAuth20Constants.TOKEN, accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value("vc+sd-jwt"))
                .andExpect(jsonPath("$.credential").exists());
        }

        @Test
        void verifyCredentialIssuanceWithOptionalClaimMissing() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS"),
                    "family_name", List.of("User"),
                    "student_id", List.of("S12345")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

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

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS"),
                    "family_name", List.of("User"),
                    "email", List.of("casuser@example.org"),
                    "student_id", List.of("S12345"),
                    "active", List.of("true"),
                    "score", List.of("95.5"),
                    "roles", List.of("admin", "user")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

            val request = new OidcVerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");
            request.setProof(buildProof(buildValidRsaProofJwt()));

            mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value("vc+sd-jwt"))
                .andExpect(jsonPath("$.credential").exists());
        }

        @Test
        void verifyCredentialIssuanceWithEcProof() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS"),
                    "family_name", List.of("User"),
                    "email", List.of("casuser@example.org"),
                    "student_id", List.of("S12345")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

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
                .andExpect(jsonPath("$.format").value("vc+sd-jwt"))
                .andExpect(jsonPath("$.credential").exists());
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

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

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

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("email", List.of("casuser@example.org")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

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

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS"),
                    "family_name", List.of("User"),
                    "email", List.of("casuser@example.org"),
                    "student_id", List.of("S12345")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

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

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS"),
                    "family_name", List.of("User"),
                    "email", List.of("casuser@example.org"),
                    "student_id", List.of("S12345")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

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

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS"),
                    "family_name", List.of("User"),
                    "email", List.of("casuser@example.org"),
                    "student_id", List.of("S12345")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

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

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS"),
                    "family_name", List.of("User"),
                    "email", List.of("casuser@example.org"),
                    "student_id", List.of("S12345")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

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

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS"),
                    "family_name", List.of("User"),
                    "email", List.of("casuser@example.org"),
                    "student_id", List.of("S12345")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

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
            val claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .audience(CREDENTIAL_ISSUER)
                .subject("casuser")
                .claim("nonce", oidcVerifiableCredentialNonceService.create().value())
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
            val claims = new JWTClaimsSet.Builder()
                .jwtID(jwtId)
                .audience(CREDENTIAL_ISSUER)
                .subject("testsubject")
                .claim("nonce", oidcVerifiableCredentialNonceService.create().value())
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
            val claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .audience(List.of("https://other.example.org", CREDENTIAL_ISSUER))
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
            val claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .audience(CREDENTIAL_ISSUER)
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
            assertNull(result.subject());
        }

        @Test
        void verifyProofWithNoJwtIdSucceeds() throws Throwable {
            val holderKey = generateRsaHolderKey();
            val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .jwk(holderKey.toPublicJWK())
                .build();
            val claims = new JWTClaimsSet.Builder()
                .audience(CREDENTIAL_ISSUER)
                .subject("casuser")
                .claim("nonce", oidcVerifiableCredentialNonceService.create().value())
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
            val claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .audience(CREDENTIAL_ISSUER)
                .subject("casuser")
                .claim("nonce", oidcVerifiableCredentialNonceService.create().value())
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
                + '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_URL;
            assertEquals(expectedEndpoint, metadata.getCredentialEndpoint());
        }

        @Test
        void verifyMetadataCredentialConfigurationsSupported() {
            val metadata = oidcCredentialIssuerMetadataService.build();
            assertNotNull(metadata.getCredentialConfigurationsSupported());
            assertFalse(metadata.getCredentialConfigurationsSupported().isEmpty());
            assertTrue(metadata.getCredentialConfigurationsSupported().containsKey("myorg"));
            val cfg = metadata.getCredentialConfigurationsSupported().get("myorg");
            assertEquals("vc+sd-jwt", cfg.getFormat());
            assertEquals("UniversityIDCredential", cfg.getScope());
        }

        @Test
        void verifyMetadataClaimsConfiguration() {
            val metadata = oidcCredentialIssuerMetadataService.build();
            val cfg = metadata.getCredentialConfigurationsSupported().get("myorg");
            assertNotNull(cfg.getClaims());
            assertEquals(7, cfg.getClaims().size());

            val givenNameClaim = cfg.getClaims().get("given_name");
            assertNotNull(givenNameClaim);
            assertTrue(givenNameClaim.getMandatory());
            assertEquals("string", givenNameClaim.getValueType());

            val familyNameClaim = cfg.getClaims().get("family_name");
            assertNotNull(familyNameClaim);
            assertTrue(familyNameClaim.getMandatory());
            assertEquals("string", familyNameClaim.getValueType());

            val emailClaim = cfg.getClaims().get("email");
            assertNotNull(emailClaim);
            assertFalse(emailClaim.getMandatory());
            assertEquals("string", emailClaim.getValueType());

            val studentIdClaim = cfg.getClaims().get("student_id");
            assertNotNull(studentIdClaim);
            assertTrue(studentIdClaim.getMandatory());
            assertEquals("string", studentIdClaim.getValueType());

            val activeClaim = cfg.getClaims().get("active");
            assertNotNull(activeClaim);
            assertFalse(activeClaim.getMandatory());
            assertEquals("boolean", activeClaim.getValueType());

            val scoreClaim = cfg.getClaims().get("score");
            assertNotNull(scoreClaim);
            assertFalse(scoreClaim.getMandatory());
            assertEquals("number", scoreClaim.getValueType());

            val rolesClaim = cfg.getClaims().get("roles");
            assertNotNull(rolesClaim);
            assertFalse(rolesClaim.getMandatory());
            assertEquals("array", rolesClaim.getValueType());
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
