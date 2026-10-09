package org.apereo.cas.vc.presentation;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.status.OidcVerifiableCredentialStatusListService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialPresentationRequestEndpointController.OidcVerifiableCredentialPresentationRequest.ClaimRequest;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialPresentationRequestEndpointController.OidcVerifiableCredentialPresentationRequest.CredentialRequest;
import com.authlete.sd.Disclosure;
import com.authlete.sd.SDJWT;
import com.authlete.sd.SDObjectBuilder;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.EllipticCurves;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcVerifiableCredentialPresentationResponseEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("OIDCWeb")
@ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
@TestPropertySource(properties = {
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.format=DC_SD_JWT",
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.scope=UniversityDegree",
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.credential-signing-alg-values-supported=ES512",
    "cas.authn.oidc.vc.issuer.status-list.enabled=true"
})
class OidcVerifiableCredentialPresentationResponseEndpointControllerTests extends AbstractOidcTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static final String PRESENTATION_RESPONSE_ENDPOINT_URL =
        "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_PRESENTATION_RESPONSE_URL;

    private static final String PRESENTATION_RESULT_ENDPOINT_URL =
        "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_PRESENTATION_RESULT_URL;

    private static final String CREDENTIAL_CONFIGURATION_ID = "UniversityDegreeCredential";

    private static final String CREDENTIAL_QUERY_ID = "university-degree";

    private static final String CREDENTIAL_CLIENT_ID = "presentation-client";

    private static final String DIGITAL_CREDENTIALS_ORIGIN = "https://wallet.example.org";

    private static final String DIGITAL_CREDENTIALS_AUDIENCE = "origin:" + DIGITAL_CREDENTIALS_ORIGIN;

    @Autowired
    @Qualifier(OidcVerifiableCredentialStatusListService.BEAN_NAME)
    private OidcVerifiableCredentialStatusListService oidcVerifiableCredentialStatusListService;

    private OidcRegisteredService credentialClient;

    @BeforeEach
    void registerCredentialClient() {
        credentialClient = getOidcRegisteredService(CREDENTIAL_CLIENT_ID,
            "https://wallet\\.example\\.org/.*", true, false);
        credentialClient.setIdTokenSigningAlg(JWSAlgorithm.ES512.getName());
        credentialClient.setJwksKeyId("EC");
        credentialClient = (OidcRegisteredService) servicesManager.save(credentialClient);
    }

    @Test
    void verifyValidPresentationAndReplayProtection() throws Throwable {
        val transaction = createTransaction();
        val material = issueCredential();
        val vpToken = buildVpToken(bindCredential(material, transaction.nonce()));

        submitPresentation(transaction.ticket().getId(), vpToken)
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
            .andExpect(header().string(HttpHeaders.PRAGMA, "no-cache"))
            .andExpect(jsonPath("$.status").value("verified"));
        assertNull(ticketRegistry.getTicket(transaction.ticket().getId()));

        assertInvalid(submitPresentation(transaction.ticket().getId(), vpToken));
    }

    @Test
    void verifyRelyingPartyCollectsTheOutcomeAndDisclosedClaims() throws Throwable {
        val transaction = createTransaction();
        val material = issueCredential();

        fetchResult(transaction.ticket().getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("pending"));

        submitPresentation(transaction.ticket().getId(), buildVpToken(bindCredential(material, transaction.nonce())))
            .andExpect(status().isOk());

        fetchResult(transaction.ticket().getId())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
            .andExpect(jsonPath("$.status").value("verified"))
            .andExpect(jsonPath("$.claims." + CREDENTIAL_QUERY_ID + ".given_name").value("Alice"));

        fetchResult(transaction.ticket().getId()).andExpect(status().isNotFound());
    }

    @Test
    void verifyWalletErrorIsReportedOnceTheRequestExpires() throws Throwable {
        val transaction = createTransaction();
        submitError(transaction.ticket().getId(), "access_denied")
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
            .andExpect(content().string("{}"));
        assertNotNull(ticketRegistry.getTicket(transaction.ticket().getId()));
        fetchResult(transaction.ticket().getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("pending"));

        ticketRegistry.deleteTicket(transaction.ticket().getId());
        fetchResult(transaction.ticket().getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("error"))
            .andExpect(jsonPath("$.error").value("access_denied"))
            .andExpect(jsonPath("$.error_description").value("The user declined"))
            .andExpect(jsonPath("$.claims").doesNotExist());
        fetchResult(transaction.ticket().getId()).andExpect(status().isNotFound());
        assertInvalid(submitError(transaction.ticket().getId(), "access_denied"));
    }

    @Test
    void verifyPresentationTakesPrecedenceOverWalletError() throws Throwable {
        val transaction = createTransaction();
        val material = issueCredential();
        submitError(transaction.ticket().getId(), "access_denied").andExpect(status().isOk());

        submitPresentation(transaction.ticket().getId(), buildVpToken(bindCredential(material, transaction.nonce())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("verified"));
        fetchResult(transaction.ticket().getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("verified"));
        fetchResult(transaction.ticket().getId()).andExpect(status().isNotFound());
    }

    @Test
    void verifyWalletErrorIsStoredBounded() throws Throwable {
        val transaction = createTransaction();
        mockMvc.perform(post(PRESENTATION_RESPONSE_ENDPOINT_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param(OAuth20Constants.ERROR, "e".repeat(5000))
                .param(OAuth20Constants.ERROR_DESCRIPTION, "d".repeat(5000))
                .param("state", transaction.ticket().getId()))
            .andExpect(status().isOk());
        ticketRegistry.deleteTicket(transaction.ticket().getId());
        fetchResult(transaction.ticket().getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.error").value("e".repeat(128)))
            .andExpect(jsonPath("$.error_description").value("d".repeat(1024)));
    }

    @Test
    void verifyEncryptedPresentation() throws Throwable {
        val transaction = createTransaction();
        val key = requireEncryptedResponse(transaction);
        val material = issueCredential();
        val vpToken = buildVpToken(bindCredential(material, transaction.nonce()));

        assertInvalid(submitPresentation(transaction.ticket().getId(), vpToken));
        assertNotNull(ticketRegistry.getTicket(transaction.ticket().getId()));

        submitEncryptedResponse(encryptResponse(key, ContentEncryptionAlgorithmIdentifiers.AES_256_GCM,
            Map.of("vp_token", MAPPER.readValue(vpToken, Map.class), OAuth20Constants.STATE, transaction.ticket().getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("verified"));
        assertNull(ticketRegistry.getTicket(transaction.ticket().getId()));
    }

    @Test
    void verifyEncryptedResponseMustMatchItsRequest() throws Throwable {
        val transaction = createTransaction();
        val key = requireEncryptedResponse(transaction);
        val other = createTransaction();
        val vpToken = MAPPER.readValue(buildVpToken(bindCredential(issueCredential(), transaction.nonce())), Map.class);

        assertInvalid(submitEncryptedResponse(encryptResponse(key, ContentEncryptionAlgorithmIdentifiers.AES_128_GCM,
            Map.of("vp_token", vpToken, OAuth20Constants.STATE, other.ticket().getId()))));
        assertInvalid(mockMvc.perform(post(PRESENTATION_RESPONSE_ENDPOINT_URL)
            .with(withHttpRequestProcessor())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("response", encryptResponse(key, ContentEncryptionAlgorithmIdentifiers.AES_128_GCM,
                Map.of("vp_token", vpToken, OAuth20Constants.STATE, transaction.ticket().getId())))
            .param(OAuth20Constants.STATE, other.ticket().getId())));
        assertInvalid(submitEncryptedResponse(encryptResponse(requireEncryptedResponse(other),
            ContentEncryptionAlgorithmIdentifiers.AES_128_GCM,
            Map.of("vp_token", vpToken, OAuth20Constants.STATE, other.ticket().getId())).replaceFirst("^[^.]+", "e30")));
        assertNotNull(ticketRegistry.getTicket(transaction.ticket().getId()));
    }

    @Test
    void verifyErrorResponseWhenEncryptionIsRequested() throws Throwable {
        val transaction = createTransaction();
        val key = requireEncryptedResponse(transaction);
        submitError(transaction.ticket().getId(), "access_denied").andExpect(status().isOk());
        submitEncryptedResponse(encryptResponse(key, ContentEncryptionAlgorithmIdentifiers.AES_128_GCM,
            Map.of(OAuth20Constants.ERROR, "access_denied", OAuth20Constants.STATE, transaction.ticket().getId())))
            .andExpect(status().isOk());
        ticketRegistry.deleteTicket(transaction.ticket().getId());
        fetchResult(transaction.ticket().getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("error"))
            .andExpect(jsonPath("$.error").value("access_denied"));
    }

    @Test
    void verifyDigitalCredentialsApiPresentation() throws Throwable {
        val transaction = createDigitalCredentialsTransaction("dc_api");
        val material = issueCredential();
        val vpToken = buildVpToken(bindCredential(material, transaction.nonce(), DIGITAL_CREDENTIALS_AUDIENCE));

        assertInvalid(submitPresentation(transaction.ticket().getId(), vpToken));
        assertInvalid(submitDigitalCredentialsResponse(transaction.ticket().getId(),
            Map.of("vp_token", MAPPER.readValue(buildVpToken(bindCredential(material, transaction.nonce())), Map.class)), credentialClient));
        assertInvalid(submitDigitalCredentialsResponse(transaction.ticket().getId(),
            Map.of("vp_token", MAPPER.readValue(vpToken, Map.class)), getOidcRegisteredService()));
        assertNotNull(ticketRegistry.getTicket(transaction.ticket().getId()));

        submitDigitalCredentialsResponse(transaction.ticket().getId(), Map.of("vp_token", MAPPER.readValue(vpToken, Map.class)), credentialClient)
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
            .andExpect(jsonPath("$.status").value("verified"))
            .andExpect(jsonPath("$.claims." + CREDENTIAL_QUERY_ID + ".given_name").value("Alice"));
        assertNull(ticketRegistry.getTicket(transaction.ticket().getId()));
        assertInvalid(submitDigitalCredentialsResponse(transaction.ticket().getId(),
            Map.of("vp_token", MAPPER.readValue(vpToken, Map.class)), credentialClient));
    }

    @Test
    void verifyEncryptedDigitalCredentialsApiPresentation() throws Throwable {
        val transaction = createDigitalCredentialsTransaction("dc_api.jwt");
        val key = requireEncryptedResponse(transaction);
        val vpToken = MAPPER.readValue(buildVpToken(bindCredential(issueCredential(), transaction.nonce(), DIGITAL_CREDENTIALS_AUDIENCE)), Map.class);

        assertInvalid(submitDigitalCredentialsResponse(transaction.ticket().getId(), Map.of("vp_token", vpToken), credentialClient));
        assertInvalid(submitEncryptedResponse(encryptResponse(key, ContentEncryptionAlgorithmIdentifiers.AES_256_GCM,
            Map.of("vp_token", vpToken, OAuth20Constants.STATE, transaction.ticket().getId()))));

        submitDigitalCredentialsResponse(transaction.ticket().getId(), Map.of("response",
                encryptResponse(key, ContentEncryptionAlgorithmIdentifiers.AES_256_GCM, Map.of("vp_token", vpToken))), credentialClient)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("verified"));
    }

    @Test
    void verifyResponseWithPresentationAndErrorIsRejected() throws Throwable {
        val transaction = createTransaction();
        assertInvalid(mockMvc.perform(post(PRESENTATION_RESPONSE_ENDPOINT_URL)
            .with(withHttpRequestProcessor())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("vp_token", "{}")
            .param(OAuth20Constants.ERROR, "access_denied")
            .param("state", transaction.ticket().getId())));
        assertNotNull(ticketRegistry.getTicket(transaction.ticket().getId()));
    }

    @Test
    void verifyUnknownPresentationResultIsNotFound() throws Throwable {
        fetchResult("TST-unknown-request").andExpect(status().isNotFound());
    }

    @Test
    void verifyPresentationResultRejectsUnauthenticatedClients() throws Throwable {
        mockMvc.perform(get(PRESENTATION_RESULT_ENDPOINT_URL)
                .with(withHttpRequestProcessor())
                .queryParam("requestId", "TST-unknown-request"))
            .andExpect(status().isUnauthorized());
    }

    private ResultActions fetchResult(final String requestId) throws Exception {
        return fetchResult(requestId, credentialClient, null);
    }

    private ResultActions fetchResult(final String requestId, final OidcRegisteredService client,
                                      final @Nullable String responseCode) throws Exception {
        val request = get(PRESENTATION_RESULT_ENDPOINT_URL)
            .with(withHttpRequestProcessor())
            .param(OAuth20Constants.CLIENT_ID, client.getClientId())
            .param(OAuth20Constants.CLIENT_SECRET, client.getClientSecrets().getFirst().getValue())
            .queryParam("requestId", requestId);
        if (responseCode != null) {
            request.queryParam("response_code", responseCode);
        }
        return mockMvc.perform(request);
    }

    @Test
    void verifyOutcomeIsReleasedOnlyToTheCreatingClient() throws Throwable {
        val transaction = createTransaction();
        val otherClient = (OidcRegisteredService) servicesManager.save(getOidcRegisteredService("other-presentation-client",
            "https://other\\.example\\.org/.*", true, false));
        fetchResult(transaction.ticket().getId(), otherClient, null).andExpect(status().isNotFound());

        submitPresentation(transaction.ticket().getId(), buildVpToken(bindCredential(issueCredential(), transaction.nonce())))
            .andExpect(status().isOk());
        fetchResult(transaction.ticket().getId(), otherClient, null).andExpect(status().isNotFound());
        fetchResult(transaction.ticket().getId()).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("verified"));
    }

    @Test
    void verifySameDeviceOutcomeRequiresTheResponseCode() throws Throwable {
        val transaction = createTransaction(List.of(claimRequest("given_name", true)), "https://wallet.example.org/callback");
        val walletResponse = MAPPER.readValue(submitPresentation(transaction.ticket().getId(),
                buildVpToken(bindCredential(issueCredential(), transaction.nonce())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("verified"))
            .andReturn().getResponse().getContentAsString(), Map.class);
        val redirectUri = walletResponse.get(OAuth20Constants.REDIRECT_URI).toString();
        assertTrue(redirectUri.startsWith("https://wallet.example.org/callback#response_code="));
        val responseCode = redirectUri.substring(redirectUri.indexOf('=') + 1);
        assertFalse(responseCode.isBlank());

        fetchResult(transaction.ticket().getId()).andExpect(status().isNotFound());
        fetchResult(transaction.ticket().getId(), credentialClient, responseCode + "x").andExpect(status().isNotFound());
        fetchResult(transaction.ticket().getId(), credentialClient, responseCode)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("verified"))
            .andExpect(jsonPath("$.responseCode").doesNotExist())
            .andExpect(jsonPath("$.clientId").doesNotExist());
    }

    @Test
    void verifyOptionalClaimsMayBeWithheld() throws Throwable {
        val withOptionalEmail = createTransaction(List.of(claimRequest("given_name", true), claimRequest("email", false)), null);
        submitPresentation(withOptionalEmail.ticket().getId(),
                buildVpToken(bindCredential(issueCredential(), withOptionalEmail.nonce())))
            .andExpect(status().isOk());

        val allOptional = createTransaction(List.of(claimRequest("email", false), claimRequest("given_name", false)), null);
        submitPresentation(allOptional.ticket().getId(), buildVpToken(bindCredential(issueCredential(), allOptional.nonce())))
            .andExpect(status().isOk());

        val noneDisclosed = createTransaction(List.of(claimRequest("email", false)), null);
        assertInvalid(submitPresentation(noneDisclosed.ticket().getId(),
            buildVpToken(bindCredential(issueCredential(), noneDisclosed.nonce()))));
    }

    @Test
    void verifyRsaAndEd25519HolderKeysAreAccepted() throws Throwable {
        val rsaHolderKey = new RSAKeyGenerator(2048).generate();
        val rsaTransaction = createTransaction();
        val rsaCredential = issueCredential(claims -> claims.setClaim("cnf", Map.of("jwk", rsaHolderKey.toPublicJWK().toJSONObject())));
        submitPresentation(rsaTransaction.ticket().getId(),
                buildVpToken(bindCredential(rsaCredential, rsaTransaction.nonce(), JWSAlgorithm.RS256, new RSASSASigner(rsaHolderKey))))
            .andExpect(status().isOk());

        val edHolderKey = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        val edPublicJwk = PublicJsonWebKey.Factory.newPublicJwk(edHolderKey.getPublic())
            .toParams(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
        val edCredential = issueCredential(claims -> claims.setClaim("cnf", Map.of("jwk", edPublicJwk)));
        for (val algorithm : List.of("Ed25519", "EdDSA")) {
            val edTransaction = createTransaction();
            submitPresentation(edTransaction.ticket().getId(),
                    buildVpToken(bindCredentialWithEd25519(edCredential, edTransaction.nonce(), algorithm, edHolderKey)))
                .andExpect(status().isOk());
        }

        val mismatched = createTransaction();
        assertInvalid(submitPresentation(mismatched.ticket().getId(),
            buildVpToken(bindCredentialWithEd25519(edCredential, mismatched.nonce(), "ES256", edHolderKey))));
    }

    @Test
    void verifyUnknownStateIsRejected() throws Exception {
        assertInvalid(submitPresentation("unknown-state", "{}"));
    }

    @Test
    void verifyUnexpectedCredentialQueryResultIsRejected() throws Throwable {
        val transaction = createTransaction();
        val vpToken = MAPPER.writeValueAsString(Map.of("unexpected-query", List.of("presentation")));

        assertInvalid(submitPresentation(transaction.ticket().getId(), vpToken));
        assertNotNull(ticketRegistry.getTicket(transaction.ticket().getId(), TransientSessionTicket.class));
    }

    @Test
    void verifyInvalidCredentialSignatureIsRejected() throws Throwable {
        val transaction = createTransaction();
        val material = issueCredential();
        val tamperedMaterial = new CredentialMaterial(material.holderKey(),
            tamperSignature(material.credentialJwt()), material.disclosures());
        val vpToken = buildVpToken(bindCredential(tamperedMaterial, transaction.nonce()));

        assertInvalid(submitPresentation(transaction.ticket().getId(), vpToken));
        assertNotNull(ticketRegistry.getTicket(transaction.ticket().getId(), TransientSessionTicket.class));
    }

    @Test
    void verifyInvalidKeyBindingNonceIsRejected() throws Throwable {
        val transaction = createTransaction();
        val material = issueCredential();
        val vpToken = buildVpToken(bindCredential(material, "invalid-nonce"));

        assertInvalid(submitPresentation(transaction.ticket().getId(), vpToken));
        assertNotNull(ticketRegistry.getTicket(transaction.ticket().getId(), TransientSessionTicket.class));
    }

    @Test
    void verifyMissingKeyBindingJwtIsRejected() throws Throwable {
        val transaction = createTransaction();
        val material = issueCredential();
        val vpToken = buildVpToken(new SDJWT(material.credentialJwt(), material.disclosures()).toString());

        assertInvalid(submitPresentation(transaction.ticket().getId(), vpToken));
        assertNotNull(ticketRegistry.getTicket(transaction.ticket().getId(), TransientSessionTicket.class));
    }

    @Test
    void verifyMissingRequestedDisclosureIsRejected() throws Throwable {
        val transaction = createTransaction();
        val material = issueCredential();
        val withoutDisclosures = new CredentialMaterial(material.holderKey(), material.credentialJwt(), List.of());
        val vpToken = buildVpToken(bindCredential(withoutDisclosures, transaction.nonce()));

        assertInvalid(submitPresentation(transaction.ticket().getId(), vpToken));
        assertNotNull(ticketRegistry.getTicket(transaction.ticket().getId(), TransientSessionTicket.class));
    }

    @Test
    void verifyCredentialWithoutExpirationIsRejected() throws Throwable {
        val transaction = createTransaction();
        val material = issueCredential(claims -> claims.unsetClaim("exp"));
        assertInvalid(submitPresentation(transaction.ticket().getId(),
            buildVpToken(bindCredential(material, transaction.nonce()))));
    }

    @Test
    void verifyForeignIssuerIsRejected() throws Throwable {
        val transaction = createTransaction();
        val material = issueCredential(claims -> claims.setIssuer("https://issuer.example.net/oidc"));
        assertInvalid(submitPresentation(transaction.ticket().getId(),
            buildVpToken(bindCredential(material, transaction.nonce()))));
    }

    @Test
    void verifyCredentialCarryingStatusIsRejected() throws Throwable {
        val transaction = createTransaction();
        val material = issueCredential(claims -> claims.setClaim("status",
            Map.of("status_list", Map.of("idx", 1, "uri", "https://issuer.example.net/statuslist"))));
        assertInvalid(submitPresentation(transaction.ticket().getId(),
            buildVpToken(bindCredential(material, transaction.nonce()))));
    }

    @Test
    void verifyCredentialStatusIsChecked() throws Throwable {
        val reference = oidcVerifiableCredentialStatusListService.allocate(getAccessToken(CREDENTIAL_CLIENT_ID), "casuser",
            CREDENTIAL_CONFIGURATION_ID, UUID.randomUUID().toString(), Duration.ofMinutes(5)).orElseThrow();
        val valid = createTransaction();
        submitPresentation(valid.ticket().getId(), buildVpToken(bindCredential(
            issueCredential(claims -> claims.setClaim("status", reference.toClaim())), valid.nonce())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("verified"));

        oidcVerifiableCredentialStatusListService.updateStatus(StringUtils.substringAfterLast(reference.uri(), "/"),
            reference.index(), OidcVerifiableCredentialStatusListService.StatusType.INVALID);
        val revoked = createTransaction();
        assertInvalid(submitPresentation(revoked.ticket().getId(), buildVpToken(bindCredential(
            issueCredential(claims -> claims.setClaim("status", reference.toClaim())), revoked.nonce()))));
    }

    private PresentationTransaction createTransaction() throws Throwable {
        return createTransaction(List.of(claimRequest("given_name", true)), null);
    }

    private PresentationTransaction createTransaction(final List<ClaimRequest> claimRequests,
                                                      final @Nullable String redirectUri) throws Throwable {
        val nonce = UUID.randomUUID().toString();
        val credentialRequest = new CredentialRequest();
        credentialRequest.setId(CREDENTIAL_QUERY_ID);
        credentialRequest.setFormat("dc+sd-jwt");
        credentialRequest.setVctValues(List.of(credentialType()));
        credentialRequest.setClaims(claimRequests);

        val factory = (TransientSessionTicketFactory) defaultTicketFactory.get(TransientSessionTicket.class);
        val ticket = factory.create(CollectionUtils.wrap(
            "nonce", nonce,
            "credentials", List.of(credentialRequest)));
        ticket.putProperty("state", ticket.getId());
        ticket.putProperty(OidcVerifiableCredentialPresentationResponseEndpointController.PROPERTY_CLIENT_ID, credentialClient.getClientId());
        if (redirectUri != null) {
            ticket.putProperty(OidcVerifiableCredentialPresentationResponseEndpointController.PROPERTY_REDIRECT_URI, redirectUri);
        }
        ticketRegistry.addTicket(ticket);
        return new PresentationTransaction(ticket, nonce);
    }

    private static ClaimRequest claimRequest(final String name, final boolean required) {
        val claimRequest = new ClaimRequest();
        claimRequest.setPath(List.of(name));
        claimRequest.setRequired(required);
        return claimRequest;
    }

    private CredentialMaterial issueCredential() throws Throwable {
        return issueCredential(claims -> {
        });
    }

    private CredentialMaterial issueCredential(final Consumer<JwtClaims> customizer) throws Throwable {
        val holderKey = new ECKeyGenerator(Curve.P_256)
            .keyID("holder-key")
            .generate();
        val disclosure = new Disclosure("given_name", "Alice");
        val sdObjectBuilder = new SDObjectBuilder();
        sdObjectBuilder.putSDClaim(disclosure);

        val claims = new JwtClaims();
        claims.setIssuer(issuer());
        claims.setSubject("casuser");
        claims.setIssuedAtToNow();
        claims.setExpirationTimeMinutesInTheFuture(5);
        claims.setNotBeforeMinutesInThePast(1);
        claims.setJwtId(UUID.randomUUID().toString());
        claims.setStringClaim("typ", "dc+sd-jwt");
        claims.setStringClaim("vct", credentialType());
        claims.setClaim("cnf", Map.of("jwk", holderKey.toPublicJWK().toJSONObject()));
        sdObjectBuilder.build().forEach(claims::setClaim);
        customizer.accept(claims);

        val credentialJwt = oidcTokenSigningAndEncryptionService.encode(credentialClient, claims);
        return new CredentialMaterial(holderKey, credentialJwt, List.of(disclosure));
    }

    private String bindCredential(final CredentialMaterial material, final String nonce) throws Exception {
        return bindCredential(material, nonce, JWSAlgorithm.ES256, new ECDSASigner(material.holderKey()));
    }

    private String bindCredential(final CredentialMaterial material, final String nonce, final String audience) throws Exception {
        val header = new JWSHeader.Builder(JWSAlgorithm.ES256)
            .type(new JOSEObjectType("kb+jwt"))
            .build();
        val bindingJwt = new SignedJWT(header, keyBindingClaims(material, nonce, audience));
        bindingJwt.sign(new ECDSASigner(material.holderKey()));
        return new SDJWT(material.credentialJwt(), material.disclosures(), bindingJwt.serialize()).toString();
    }

    private String bindCredential(final CredentialMaterial material, final String nonce,
                                  final JWSAlgorithm algorithm, final JWSSigner signer) throws Exception {
        val header = new JWSHeader.Builder(algorithm)
            .type(new JOSEObjectType("kb+jwt"))
            .build();
        val bindingJwt = new SignedJWT(header, keyBindingClaims(material, nonce));
        bindingJwt.sign(signer);
        return new SDJWT(material.credentialJwt(), material.disclosures(), bindingJwt.serialize()).toString();
    }

    private String bindCredentialWithEd25519(final CredentialMaterial material, final String nonce,
                                             final String algorithm, final KeyPair holderKey) throws Exception {
        val header = Base64URL.encode("{\"alg\":\"%s\",\"typ\":\"kb+jwt\"}".formatted(algorithm));
        val payload = Base64URL.encode(keyBindingClaims(material, nonce).toString());
        val signingInput = header + "." + payload;
        val signature = java.security.Signature.getInstance("Ed25519");
        signature.initSign(holderKey.getPrivate());
        signature.update(signingInput.getBytes(StandardCharsets.US_ASCII));
        val bindingJwt = signingInput + '.' + Base64URL.encode(signature.sign());
        return new SDJWT(material.credentialJwt(), material.disclosures(), bindingJwt).toString();
    }

    private JWTClaimsSet keyBindingClaims(final CredentialMaterial material, final String nonce) {
        return keyBindingClaims(material, nonce, verifierClientId());
    }

    private JWTClaimsSet keyBindingClaims(final CredentialMaterial material, final String nonce, final String audience) {
        val unboundSdJwt = new SDJWT(material.credentialJwt(), material.disclosures());
        return new JWTClaimsSet.Builder()
            .issueTime(new Date())
            .audience(audience)
            .claim("nonce", nonce)
            .claim("sd_hash", unboundSdJwt.getSDHash())
            .build();
    }

    private static String tamperSignature(final String jwt) {
        val signatureStart = jwt.lastIndexOf('.') + 1;
        val replacement = jwt.charAt(signatureStart) == 'A' ? 'B' : 'A';
        return jwt.substring(0, signatureStart) + replacement + jwt.substring(signatureStart + 1);
    }

    private static void assertInvalid(final ResultActions result) throws Exception {
        result
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
            .andExpect(header().string(HttpHeaders.PRAGMA, "no-cache"))
            .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST))
            .andExpect(jsonPath("$.error_description")
                .value("The presentation response could not be validated"));
    }

    private PublicJsonWebKey requireEncryptedResponse(final PresentationTransaction transaction) throws Exception {
        val key = EcJwkGenerator.generateJwk(EllipticCurves.P256);
        key.setKeyId(transaction.ticket().getId());
        key.setAlgorithm(KeyManagementAlgorithmIdentifiers.ECDH_ES);
        transaction.ticket().putProperty(OidcVerifiableCredentialPresentationResponseEndpointController.PROPERTY_RESPONSE_ENCRYPTION_KEY,
            key.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE));
        ticketRegistry.updateTicket(transaction.ticket());
        return key;
    }

    private static String encryptResponse(final PublicJsonWebKey key, final String contentEncryption,
                                          final Map<String, Object> parameters) throws Exception {
        val jwe = new JsonWebEncryption();
        jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.ECDH_ES);
        jwe.setEncryptionMethodHeaderParameter(contentEncryption);
        jwe.setKeyIdHeaderValue(key.getKeyId());
        jwe.setKey(key.getPublicKey());
        jwe.setPayload(MAPPER.writeValueAsString(parameters));
        return jwe.getCompactSerialization();
    }

    private PresentationTransaction createDigitalCredentialsTransaction(final String responseMode) throws Throwable {
        val transaction = createTransaction();
        transaction.ticket().putProperty(OidcVerifiableCredentialPresentationResponseEndpointController.PROPERTY_RESPONSE_MODE, responseMode);
        transaction.ticket().putProperty(OidcVerifiableCredentialPresentationResponseEndpointController.PROPERTY_ORIGIN, DIGITAL_CREDENTIALS_ORIGIN);
        ticketRegistry.updateTicket(transaction.ticket());
        return transaction;
    }

    private ResultActions submitDigitalCredentialsResponse(final String requestId, final Map<String, Object> data,
                                                           final OidcRegisteredService client) throws Exception {
        return mockMvc.perform(post(PRESENTATION_RESULT_ENDPOINT_URL)
            .with(withHttpRequestProcessor())
            .param(OAuth20Constants.CLIENT_ID, client.getClientId())
            .param(OAuth20Constants.CLIENT_SECRET, client.getClientSecrets().getFirst().getValue())
            .contentType(MediaType.APPLICATION_JSON)
            .content(MAPPER.writeValueAsString(Map.of("request_id", requestId, "data", data))));
    }

    private ResultActions submitEncryptedResponse(final String response) throws Exception {
        return mockMvc.perform(post(PRESENTATION_RESPONSE_ENDPOINT_URL)
            .with(withHttpRequestProcessor())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("response", response));
    }

    private ResultActions submitPresentation(final String state, final String vpToken) throws Exception {
        return mockMvc.perform(post(PRESENTATION_RESPONSE_ENDPOINT_URL)
            .with(withHttpRequestProcessor())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("vp_token", vpToken)
            .param("state", state));
    }

    private ResultActions submitError(final String state, final String error) throws Exception {
        return mockMvc.perform(post(PRESENTATION_RESPONSE_ENDPOINT_URL)
            .with(withHttpRequestProcessor())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param(OAuth20Constants.ERROR, error)
            .param(OAuth20Constants.ERROR_DESCRIPTION, "The user declined")
            .param("state", state));
    }

    private String buildVpToken(final String presentation) throws Exception {
        return MAPPER.writeValueAsString(Map.of(CREDENTIAL_QUERY_ID, List.of(presentation)));
    }

    private String issuer() {
        return casProperties.getAuthn().getOidc().getCore().getIssuer();
    }

    private String credentialType() {
        return issuer() + '/' + OidcConstants.VC_CREDENTIAL_TYPE_URL + '/' + CREDENTIAL_CONFIGURATION_ID;
    }

    private String verifierClientId() {
        return "redirect_uri:" + issuer() + '/' + OidcConstants.VC_PRESENTATION_RESPONSE_URL;
    }

    private record PresentationTransaction(TransientSessionTicket ticket, String nonce) {
    }

    private record CredentialMaterial(ECKey holderKey, String credentialJwt, List<Disclosure> disclosures) {
    }
}
