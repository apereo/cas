package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.config.CasStatelessTicketRegistryAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.status.OidcVerifiableCredentialStatusEndpoint;
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
import com.nimbusds.jose.util.Base64URL;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
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
    "cas.authn.oidc.vc.issuer.credential-configurations.DriverLicenseCredential.scope=DriverLicense",
    "cas.authn.oidc.vc.issuer.status-list.enabled=true",
    "management.endpoints.web.exposure.include=oidcVcStatus",
    "management.endpoint.oidcVcStatus.access=UNRESTRICTED"
})
class OidcVerifiableCredentialIssuanceTests extends AbstractOidcTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static final String OFFER_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_OFFER_URL;

    private static final String TRANSACTIONS_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_OFFER_TRANSACTIONS_URL;

    private static final String TOKEN_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL;

    private static final String NONCE_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_NONCE_URL;

    private static final String CREDENTIAL_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_URL;

    private static final String STATUS_LIST_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_STATUS_LIST_URL;

    private static final String CREDENTIAL_ISSUER = "https://sso.example.org/cas/oidc";

    private static final JOSEObjectType PROOF_JWT_TYPE = new JOSEObjectType("openid4vci-proof+jwt");

    @Autowired
    @Qualifier("oidcVerifiableCredentialStatusEndpoint")
    private OidcVerifiableCredentialStatusEndpoint oidcVerifiableCredentialStatusEndpoint;

    @Test
    void verifyIssuedCredentialStatusCanBeChanged() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        val credential = issueCredential(mockMvc, registeredService.getClientId(), registeredService.getClientSecrets().getFirst().getValue());
        val claims = SignedJWT.parse(StringUtils.substringBefore(credential, "~")).getJWTClaimsSet();
        val statusList = assertInstanceOf(Map.class, claims.getJSONObjectClaim("status").get("status_list"));
        val uri = statusList.get("uri").toString();
        val index = ((Number) statusList.get("idx")).longValue();
        assertTrue(uri.startsWith(CREDENTIAL_ISSUER + '/' + OidcConstants.VC_STATUS_LIST_URL + '/'));
        val statusListId = StringUtils.substringAfterLast(uri, "/");
        assertEquals(0, readStatus(statusListId, index));

        val entry = oidcVerifiableCredentialStatusEndpoint.getEntries(claims.getSubject()).stream()
            .filter(candidate -> candidate.credentialId().equals(claims.getJWTID()))
            .findFirst()
            .orElseThrow();
        assertEquals(index, entry.index());
        assertEquals(registeredService.getClientId(), entry.clientId());
        assertEquals(200, oidcVerifiableCredentialStatusEndpoint.updateStatus(statusListId, index, "invalid").getStatus());
        assertEquals(1, readStatus(statusListId, index));
        assertEquals(200, oidcVerifiableCredentialStatusEndpoint.updateStatus(statusListId, index, "SUSPENDED").getStatus());
        assertEquals(2, readStatus(statusListId, index));
        assertEquals(400, oidcVerifiableCredentialStatusEndpoint.updateStatus(statusListId, index, "unknown").getStatus());
        assertEquals(404, oidcVerifiableCredentialStatusEndpoint.updateStatus("9999", index, "VALID").getStatus());
        mockMvc.perform(get(STATUS_LIST_URL + "/9999").with(withHttpRequestProcessor())).andExpect(status().isNotFound());
        mockMvc.perform(get(STATUS_LIST_URL + "/unknown").with(withHttpRequestProcessor())).andExpect(status().isNotFound());
    }

    private int readStatus(final String statusListId, final long index) throws Exception {
        val response = mockMvc.perform(get(STATUS_LIST_URL + '/' + statusListId).with(withHttpRequestProcessor()))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/statuslist+jwt"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
            .andReturn().getResponse().getContentAsString();
        val token = SignedJWT.parse(response);
        assertEquals("statuslist+jwt", token.getHeader().getType().toString());
        val claims = token.getJWTClaimsSet();
        assertEquals(CREDENTIAL_ISSUER + '/' + OidcConstants.VC_STATUS_LIST_URL + '/' + statusListId, claims.getSubject());
        assertNotNull(claims.getIssueTime());
        assertTrue(claims.getExpirationTime().after(claims.getIssueTime()));
        assertEquals(600L, claims.getLongClaim("ttl"));
        val statusList = claims.getJSONObjectClaim("status_list");
        assertEquals(2L, ((Number) statusList.get("bits")).longValue());
        val inflater = new Inflater();
        inflater.setInput(new Base64URL(statusList.get("lst").toString()).decode());
        val statuses = new byte[131_072 / 4];
        assertEquals(statuses.length, inflater.inflate(statuses));
        inflater.end();
        return statuses[(int) (index / 4)] >> (int) (index % 4 * 2) & 0b11;
    }

    private static String issueCredential(final MockMvc mockMvc, final String clientId, final String clientSecret) throws Exception {
        val transaction = createOfferTransaction(mockMvc, clientId, clientSecret, "casuser", List.of("UniversityDegreeCredential"));
        val preAuthorizedCode = fetchPreAuthorizedCode(mockMvc, transaction.transactionId());
        val tokenResponseBody = mockMvc.perform(tokenExchangeRequest(clientId, clientSecret, preAuthorizedCode, transaction.txCode()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        val accessToken = JsonPath.read(tokenResponseBody, "$." + OAuth20Constants.ACCESS_TOKEN).toString();
        val nonceResponseBody = mockMvc.perform(post(NONCE_URL).with(withHttpRequestProcessor()).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        val credentialRequest = new OidcVerifiableCredentialRequest();
        credentialRequest.setCredentialConfigurationId("UniversityDegreeCredential");
        credentialRequest.setProofs(buildProofs(buildProofJwt(JsonPath.read(nonceResponseBody, "$." + OidcConstants.C_NONCE).toString())));
        val credentialResponseBody = mockMvc.perform(post(CREDENTIAL_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .content(MAPPER.writeValueAsString(credentialRequest)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(credentialResponseBody, "$.credentials[0].credential").toString();
    }

    @Test
    void verifyPreAuthorizedCodeExchangeRequiresTransactionCode() throws Exception {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);

        val preAuthorizedCode = createOfferAndFetchPreAuthorizedCode(mockMvc,
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

        val transaction = createOfferTransaction(mockMvc,
            registeredService.getClientId(), registeredService.getClientSecrets().getFirst().getValue(),
            "casuser", List.of("UniversityDegreeCredential"));
        val preAuthorizedCode = fetchPreAuthorizedCode(mockMvc, transaction.transactionId());

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

        val transaction = createOfferTransaction(mockMvc,
            registeredService.getClientId(), registeredService.getClientSecrets().getFirst().getValue(),
            "casuser", List.of("UniversityDegreeCredential"));
        val preAuthorizedCode = fetchPreAuthorizedCode(mockMvc, transaction.transactionId());

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
        credentialRequest.setProofs(buildProofs(buildProofJwt(nonce)));

        mockMvc.perform(post(CREDENTIAL_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .content(MAPPER.writeValueAsString(credentialRequest)))
            .andExpect(status().is4xxClientError());
    }

    /**
     * The stateless registry cannot delete, so the pre-authorized code and the nonce are not single use there;
     * everything else the issuance needs must survive being encoded into the tickets themselves.
     */
    @Nested
    @ImportAutoConfiguration({
        CasOidcVerifiableCredentialsAutoConfiguration.class,
        CasStatelessTicketRegistryAutoConfiguration.class
    })
    class StatelessTicketRegistryTests extends AbstractOidcTests {
        @Test
        void verifyPreAuthorizedCodeIssuance() throws Exception {
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            servicesManager.save(registeredService);
            val clientSecret = registeredService.getClientSecrets().getFirst().getValue();

            val transaction = createOfferTransaction(mockMvc, registeredService.getClientId(), clientSecret,
                "casuser", List.of("UniversityDegreeCredential"));
            val preAuthorizedCode = fetchPreAuthorizedCode(mockMvc, transaction.transactionId());

            val tokenResponseBody = mockMvc.perform(tokenExchangeRequest(registeredService.getClientId(),
                    clientSecret, preAuthorizedCode, transaction.txCode()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
            val accessToken = JsonPath.read(tokenResponseBody, "$." + OAuth20Constants.ACCESS_TOKEN).toString();
            mockMvc.perform(tokenExchangeRequest(registeredService.getClientId(),
                    clientSecret, preAuthorizedCode, transaction.txCode()))
                .andExpect(status().isOk());

            val nonceResponseBody = mockMvc.perform(post(NONCE_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
            val nonce = JsonPath.read(nonceResponseBody, "$." + OidcConstants.C_NONCE).toString();

            val credentialRequest = new OidcVerifiableCredentialRequest();
            credentialRequest.setCredentialConfigurationId("UniversityDegreeCredential");
            for (var attempt = 0; attempt < 2; attempt++) {
                credentialRequest.setProofs(buildProofs(buildProofJwt(nonce)));
                val credentialResponseBody = mockMvc.perform(post(CREDENTIAL_URL)
                        .with(withHttpRequestProcessor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .content(MAPPER.writeValueAsString(credentialRequest)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
                val credential = JsonPath.read(credentialResponseBody, "$.credentials[0].credential").toString();
                assertNull(SignedJWT.parse(StringUtils.substringBefore(credential, "~")).getJWTClaimsSet().getClaim("status"));
            }

            credentialRequest.setCredentialConfigurationId("DriverLicenseCredential");
            credentialRequest.setProofs(buildProofs(buildProofJwt(nonce)));
            mockMvc.perform(post(CREDENTIAL_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .content(MAPPER.writeValueAsString(credentialRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_CREDENTIAL_REQUEST_DENIED));
        }
    }

    private static OfferTransaction createOfferTransaction(final MockMvc mockMvc, final String clientId, final String clientSecret,
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

    private static String fetchPreAuthorizedCode(final MockMvc mockMvc, final String transactionId) throws Exception {
        val responseBody = mockMvc.perform(get(OFFER_URL + '/' + transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(responseBody,
            "$.grants.['urn:ietf:params:oauth:grant-type:pre-authorized_code'].pre-authorized_code").toString();
    }

    private static String createOfferAndFetchPreAuthorizedCode(final MockMvc mockMvc, final String clientId, final String clientSecret,
                                                               final String principal, final List<String> credentialConfigurationIds) throws Exception {
        val transaction = createOfferTransaction(mockMvc, clientId, clientSecret, principal, credentialConfigurationIds);
        return fetchPreAuthorizedCode(mockMvc, transaction.transactionId());
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

    private static OidcVerifiableCredentialRequest.Proofs buildProofs(final String... jwts) {
        val proofs = new OidcVerifiableCredentialRequest.Proofs();
        proofs.setJwt(List.of(jwts));
        return proofs;
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
