package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcVerifiableCredentialIssuanceTests}.
 * <p>
 * These tests treat CAS as an external outsider would: only the public OIDC/OpenID4VCI HTTP
 * endpoints are exercised via {@code MockMvc}, chaining the credential-offer, token and credential
 * endpoints exactly as a malicious OAuth/OIDC client would.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("OIDCWeb")
@ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
@TestPropertySource(properties = {
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.format=DC_SD_JWT",
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.scope=UniversityDegree",
    "cas.authn.oidc.vc.issuer.credential-configurations.DriverLicenseCredential.format=DC_SD_JWT",
    "cas.authn.oidc.vc.issuer.credential-configurations.DriverLicenseCredential.scope=DriverLicense"
})
class OidcVerifiableCredentialIssuanceTests extends AbstractOidcTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static final String OFFER_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_OFFER_URL;

    private static final String TRANSACTIONS_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_OFFER_TRANSACTIONS_URL;

    private static final String TOKEN_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL;

    private static final String NONCE_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_NONCE_URL;

    private static final String CREDENTIAL_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_URL;

    private static final String CREDENTIAL_ISSUER = "https://sso.example.org/cas/oidc";

    private static final JOSEObjectType PROOF_JWT_TYPE = new JOSEObjectType("openid4vci-proof+jwt");

    @Test
    void verifyPreAuthorizedCodeExchangeRequiresTransactionCode() throws Exception {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);

        val preAuthorizedCode = createOfferAndFetchPreAuthorizedCode(
            registeredService.getClientId(), registeredService.getClientSecrets().getFirst().getValue(),
            "casuser", List.of("UniversityDegreeCredential"));

        mockMvc.perform(post(TOKEN_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecrets().getFirst().getValue())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PRE_AUTHORIZED_CODE.getType())
                .queryParam(OidcConstants.PRE_AUTHORIZED_CODE, preAuthorizedCode))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void verifyPreAuthorizedCodeCannotBeRedeemedMoreThanOnce() throws Exception {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);

        val transaction = createOfferTransaction(
            registeredService.getClientId(), registeredService.getClientSecrets().getFirst().getValue(),
            "casuser", List.of("UniversityDegreeCredential"));
        val preAuthorizedCode = fetchPreAuthorizedCode(transaction.transactionId());

        mockMvc.perform(tokenExchangeRequest(registeredService.getClientId(),
                registeredService.getClientSecrets().getFirst().getValue(), preAuthorizedCode, transaction.txCode()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$." + OAuth20Constants.ACCESS_TOKEN).exists());

        mockMvc.perform(tokenExchangeRequest(registeredService.getClientId(),
                registeredService.getClientSecrets().getFirst().getValue(), preAuthorizedCode, transaction.txCode()))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void verifyCredentialEndpointRejectsConfigurationNotAuthorizedByToken() throws Exception {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);

        val transaction = createOfferTransaction(
            registeredService.getClientId(), registeredService.getClientSecrets().getFirst().getValue(),
            "casuser", List.of("UniversityDegreeCredential"));
        val preAuthorizedCode = fetchPreAuthorizedCode(transaction.transactionId());

        val tokenResponseBody = mockMvc.perform(tokenExchangeRequest(registeredService.getClientId(),
                registeredService.getClientSecrets().getFirst().getValue(), preAuthorizedCode, transaction.txCode()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        val accessToken = JsonPath.read(tokenResponseBody, "$." + OAuth20Constants.ACCESS_TOKEN).toString();

        val nonceResponseBody = mockMvc.perform(post(NONCE_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        val nonce = JsonPath.read(nonceResponseBody, "$." + OidcConstants.C_NONCE).toString();

        val credentialRequest = new OidcVerifiableCredentialRequest();
        credentialRequest.setCredentialConfigurationId("DriverLicenseCredential");
        credentialRequest.setProof(buildProof(buildProofJwt(nonce)));

        mockMvc.perform(post(CREDENTIAL_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .content(MAPPER.writeValueAsString(credentialRequest)))
            .andExpect(status().is4xxClientError());
    }

    private OfferTransaction createOfferTransaction(final String clientId, final String clientSecret,
                                                    final String principal, final List<String> credentialConfigurationIds) throws Exception {
        val requestBody = MAPPER.writeValueAsString(
            Map.of("principal", principal, "credentialConfigurationIds", credentialConfigurationIds));
        val responseBody = mockMvc.perform(post(TRANSACTIONS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(withHttpRequestProcessor())
                .param(OAuth20Constants.CLIENT_ID, clientId)
                .param(OAuth20Constants.CLIENT_SECRET, clientSecret))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return new OfferTransaction(
            JsonPath.read(responseBody, "$.transactionId").toString(),
            JsonPath.read(responseBody, "$.txCode").toString());
    }

    private String fetchPreAuthorizedCode(final String transactionId) throws Exception {
        val responseBody = mockMvc.perform(get(OFFER_URL + '/' + transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(responseBody,
            "$.grants.['urn:ietf:params:oauth:grant-type:pre-authorized_code'].pre-authorized_code").toString();
    }

    private String createOfferAndFetchPreAuthorizedCode(final String clientId, final String clientSecret,
                                                        final String principal, final List<String> credentialConfigurationIds) throws Exception {
        val transaction = createOfferTransaction(clientId, clientSecret, principal, credentialConfigurationIds);
        return fetchPreAuthorizedCode(transaction.transactionId());
    }

    private record OfferTransaction(String transactionId, String txCode) {
    }

    private static MockHttpServletRequestBuilder tokenExchangeRequest(final String clientId, final String clientSecret,
                                                                      final String preAuthorizedCode, final String txCode) {
        return post(TOKEN_URL)
            .secure(true)
            .with(withHttpRequestProcessor())
            .param(OAuth20Constants.CLIENT_ID, clientId)
            .param(OAuth20Constants.CLIENT_SECRET, clientSecret)
            .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PRE_AUTHORIZED_CODE.getType())
            .queryParam(OidcConstants.PRE_AUTHORIZED_CODE, preAuthorizedCode)
            .queryParam(OidcConstants.TX_CODE, txCode);
    }

    private static OidcVerifiableCredentialRequest.Proof buildProof(final String jwt) {
        val proof = new OidcVerifiableCredentialRequest.Proof();
        proof.setProofType("jwt");
        proof.setJwt(jwt);
        return proof;
    }

    private static String buildProofJwt(final String nonce) throws Exception {
        val holderKey = new RSAKeyGenerator(2048).keyID("holder-rsa").generate();
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
        signedJwt.sign(new RSASSASigner((RSAKey) holderKey));
        return signedJwt.serialize();
    }
}
