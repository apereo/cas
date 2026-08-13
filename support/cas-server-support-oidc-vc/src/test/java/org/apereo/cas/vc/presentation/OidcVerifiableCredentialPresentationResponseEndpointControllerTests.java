package org.apereo.cas.vc.presentation;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
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
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.credential-signing-alg-values-supported=ES512"
})
class OidcVerifiableCredentialPresentationResponseEndpointControllerTests extends AbstractOidcTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static final String PRESENTATION_RESPONSE_ENDPOINT_URL =
        "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_PRESENTATION_RESPONSE_URL;

    private static final String CREDENTIAL_CONFIGURATION_ID = "UniversityDegreeCredential";

    private static final String CREDENTIAL_QUERY_ID = "university-degree";

    private static final String CREDENTIAL_CLIENT_ID = "presentation-client";

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

    private PresentationTransaction createTransaction() throws Throwable {
        val nonce = UUID.randomUUID().toString();
        val claimRequest = new ClaimRequest();
        claimRequest.setPath(List.of("given_name"));
        claimRequest.setRequired(true);

        val credentialRequest = new CredentialRequest();
        credentialRequest.setId(CREDENTIAL_QUERY_ID);
        credentialRequest.setFormat("dc+sd-jwt");
        credentialRequest.setVctValues(List.of(credentialType()));
        credentialRequest.setClaims(List.of(claimRequest));

        val factory = (TransientSessionTicketFactory) defaultTicketFactory.get(TransientSessionTicket.class);
        val ticket = factory.create(CollectionUtils.wrap(
            "nonce", nonce,
            "credentials", List.of(credentialRequest)));
        ticket.putProperty("state", ticket.getId());
        ticketRegistry.addTicket(ticket);
        return new PresentationTransaction(ticket, nonce);
    }

    private CredentialMaterial issueCredential() throws Throwable {
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
        claims.setStringClaim("client_id", CREDENTIAL_CLIENT_ID);
        claims.setStringClaim("credential_configuration_id", CREDENTIAL_CONFIGURATION_ID);
        claims.setClaim("cnf", Map.of("jwk", holderKey.toPublicJWK().toJSONObject()));
        sdObjectBuilder.build().forEach(claims::setClaim);

        val credentialJwt = oidcTokenSigningAndEncryptionService.encode(credentialClient, claims);
        return new CredentialMaterial(holderKey, credentialJwt, List.of(disclosure));
    }

    private String bindCredential(final CredentialMaterial material, final String nonce) throws Exception {
        val unboundSdJwt = new SDJWT(material.credentialJwt(), material.disclosures());
        val claims = new JWTClaimsSet.Builder()
            .issueTime(new Date())
            .audience(verifierClientId())
            .claim("nonce", nonce)
            .claim("sd_hash", unboundSdJwt.getSDHash())
            .build();
        val header = new JWSHeader.Builder(JWSAlgorithm.ES256)
            .type(new JOSEObjectType("kb+jwt"))
            .build();
        val bindingJwt = new SignedJWT(header, claims);
        bindingJwt.sign(new ECDSASigner(material.holderKey()));
        return new SDJWT(material.credentialJwt(), material.disclosures(), bindingJwt.serialize()).toString();
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

    private ResultActions submitPresentation(final String state, final String vpToken) throws Exception {
        return mockMvc.perform(post(PRESENTATION_RESPONSE_ENDPOINT_URL)
            .with(withHttpRequestProcessor())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("vp_token", vpToken)
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
