package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.config.CasStatelessTicketRegistryAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.deferred.OidcVerifiableCredentialDeferredIssuanceEndpoint;
import org.apereo.cas.oidc.vc.issuer.status.OidcVerifiableCredentialStatusEndpoint;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.ECDHDecrypter;
import com.nimbusds.jose.crypto.ECDHEncrypter;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
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
    "cas.authn.oidc.vc.issuer.credential-configurations.DeferredCredential.format=DC_SD_JWT",
    "cas.authn.oidc.vc.issuer.credential-configurations.DeferredCredential.scope=Deferred",
    "cas.authn.oidc.vc.issuer.credential-configurations.DeferredCredential.deferred-issuance=true",
    "cas.authn.oidc.vc.issuer.credential-configurations.DeferredReviewCredential.format=DC_SD_JWT",
    "cas.authn.oidc.vc.issuer.credential-configurations.DeferredReviewCredential.scope=DeferredReview",
    "cas.authn.oidc.vc.issuer.credential-configurations.DeferredReviewCredential.deferred-issuance=true",
    "cas.authn.oidc.vc.issuer.credential-configurations.DeferredReviewCredential.claims.license_number.mandatory=true",
    "cas.authn.oidc.vc.issuer.status-list.enabled=true",
    "management.endpoints.web.exposure.include=oidcVcStatus,oidcVcDeferred",
    "management.endpoint.oidcVcStatus.access=UNRESTRICTED",
    "management.endpoint.oidcVcDeferred.access=UNRESTRICTED"
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

    private static final String DEFERRED_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_DEFERRED_CREDENTIAL_URL;

    private static final String METADATA_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.WELL_KNOWN_OPENID_CREDENTIAL_ISSUER_URL;

    private static final MediaType APPLICATION_JWT = MediaType.parseMediaType(OidcConstants.CONTENT_TYPE_JWT);

    private static final String CREDENTIAL_ISSUER = "https://sso.example.org/cas/oidc";

    private static final JOSEObjectType PROOF_JWT_TYPE = new JOSEObjectType("openid4vci-proof+jwt");

    @Autowired
    @Qualifier("oidcVerifiableCredentialStatusEndpoint")
    private OidcVerifiableCredentialStatusEndpoint oidcVerifiableCredentialStatusEndpoint;

    @Autowired
    private ObjectProvider<OidcVerifiableCredentialDeferredIssuanceEndpoint> oidcVerifiableCredentialDeferredIssuanceEndpoint;

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

        val aggregationUri = CREDENTIAL_ISSUER + '/' + OidcConstants.VC_STATUS_LIST_AGGREGATION_URL;
        mockMvc.perform(get("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_STATUS_LIST_AGGREGATION_URL)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
            .andExpect(jsonPath("$.status_lists[?(@ == '%s')]".formatted(uri)).exists());
        mockMvc.perform(get("/cas/" + OidcConstants.BASE_OIDC_URL + "/.well-known/openid-configuration").with(withHttpRequestProcessor()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status_list_aggregation_endpoint").value(aggregationUri));
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
        assertEquals(CREDENTIAL_ISSUER + '/' + OidcConstants.VC_STATUS_LIST_AGGREGATION_URL, statusList.get("aggregation_uri"));
        val inflater = new Inflater();
        inflater.setInput(new Base64URL(statusList.get("lst").toString()).decode());
        val statuses = new byte[131_072 / 4];
        assertEquals(statuses.length, inflater.inflate(statuses));
        inflater.end();
        return statuses[(int) (index / 4)] >> (int) (index % 4 * 2) & 0b11;
    }

    private static String issueCredential(final MockMvc mockMvc, final String clientId, final String clientSecret) throws Exception {
        val accessToken = obtainAccessToken(mockMvc, clientId, clientSecret);
        val credentialResponseBody = mockMvc.perform(post(CREDENTIAL_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .content(MAPPER.writeValueAsString(buildCredentialRequest(fetchNonce(mockMvc), null))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(credentialResponseBody, "$.credentials[0].credential").toString();
    }

    private static String obtainAccessToken(final MockMvc mockMvc, final String clientId, final String clientSecret) throws Exception {
        return obtainAccessToken(mockMvc, clientId, clientSecret, "UniversityDegreeCredential");
    }

    private static String obtainAccessToken(final MockMvc mockMvc, final String clientId, final String clientSecret,
                                            final String credentialConfigurationId)
            throws Exception {
        val transaction = createOfferTransaction(mockMvc, clientId, clientSecret, "casuser", List.of(credentialConfigurationId));
        val preAuthorizedCode = fetchPreAuthorizedCode(mockMvc, transaction.transactionId());
        val tokenResponseBody = mockMvc.perform(tokenExchangeRequest(clientId, clientSecret, preAuthorizedCode, transaction.txCode()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(tokenResponseBody, "$." + OAuth20Constants.ACCESS_TOKEN).toString();
    }

    private static String fetchNonce(final MockMvc mockMvc) throws Exception {
        val nonceResponseBody = mockMvc.perform(post(NONCE_URL).with(withHttpRequestProcessor()).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(nonceResponseBody, "$." + OidcConstants.C_NONCE).toString();
    }

    private static OidcVerifiableCredentialRequest buildCredentialRequest(final String nonce, final JWK responseEncryptionKey,
                                                                          final String encryptionMethod)
            throws Exception {
        val credentialRequest = buildCredentialRequest(nonce, null);
        val responseEncryption = new OidcVerifiableCredentialRequest.CredentialResponseEncryption();
        responseEncryption.setJwk(responseEncryptionKey.toPublicJWK().toJSONObject());
        responseEncryption.setEnc(encryptionMethod);
        credentialRequest.setCredentialResponseEncryption(responseEncryption);
        return credentialRequest;
    }

    private static OidcVerifiableCredentialRequest buildCredentialRequest(final String nonce,
        final OidcVerifiableCredentialRequest.CredentialResponseEncryption responseEncryption)
            throws Exception {
        val credentialRequest = new OidcVerifiableCredentialRequest();
        credentialRequest.setCredentialConfigurationId("UniversityDegreeCredential");
        credentialRequest.setProofs(buildProofs(buildProofJwt(nonce)));
        credentialRequest.setCredentialResponseEncryption(responseEncryption);
        return credentialRequest;
    }

    private static String encryptRequest(final OidcVerifiableCredentialRequest request, final JWK issuerKey,
                                         final EncryptionMethod encryptionMethod)
            throws Exception {
        val header = new JWEHeader.Builder(JWEAlgorithm.parse(issuerKey.getAlgorithm().getName()), encryptionMethod)
            .keyID(issuerKey.getKeyID())
            .build();
        val jwe = new JWEObject(header, new Payload(MAPPER.writeValueAsString(request)));
        jwe.encrypt(issuerKey instanceof final ECKey ecKey ? new ECDHEncrypter(ecKey) : new RSAEncrypter((RSAKey) issuerKey));
        return jwe.serialize();
    }

    private static JWK generateWalletKey(final JWK issuerKey) throws Exception {
        if (issuerKey instanceof ECKey) {
            return new ECKeyGenerator(Curve.P_256).keyID("wallet-ec").algorithm(JWEAlgorithm.ECDH_ES).keyUse(KeyUse.ENCRYPTION).generate();
        }
        return new RSAKeyGenerator(2048).keyID("wallet-rsa").algorithm(JWEAlgorithm.RSA_OAEP_256).generate();
    }

    private static List<JWK> fetchRequestEncryptionKeys(final MockMvc mockMvc) throws Exception {
        val metadata = mockMvc.perform(get(METADATA_URL).with(withHttpRequestProcessor()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        val jwks = JsonPath.<Map<String, Object>>read(metadata, "$.credential_request_encryption.jwks");
        return JWKSet.parse(jwks).getKeys();
    }

    private static ResultActions postCredentialRequest(final MockMvc mockMvc, final String accessToken,
                                                       final MediaType contentType, final String body)
            throws Exception {
        return postCredentialRequest(mockMvc, accessToken, contentType, body, CREDENTIAL_URL);
    }

    private static ResultActions postCredentialRequest(final MockMvc mockMvc, final String accessToken,
                                                       final MediaType contentType, final String body, final String url)
            throws Exception {
        return mockMvc.perform(post(url)
            .with(withHttpRequestProcessor())
            .contentType(contentType)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .content(body));
    }

    @Test
    void verifyDeferredIssuance() throws Throwable {
        mockMvc.perform(get(METADATA_URL).with(withHttpRequestProcessor()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.deferred_credential_endpoint").value(CREDENTIAL_ISSUER + '/' + OidcConstants.VC_DEFERRED_CREDENTIAL_URL));
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        val accessToken = obtainAccessToken(mockMvc, registeredService.getClientId(),
            registeredService.getClientSecrets().getFirst().getValue(), "DeferredCredential");
        val transactionId = requestDeferredCredential(accessToken);
        val endpoint = oidcVerifiableCredentialDeferredIssuanceEndpoint.getObject();
        val transaction = endpoint.getTransactions(null).stream()
            .filter(candidate -> candidate.transactionId().equals(transactionId)).findFirst().orElseThrow();
        assertEquals("PENDING", transaction.status().name());
        assertEquals(registeredService.getClientId(), transaction.clientId());
        assertEquals(1, transaction.getCredentials());

        postDeferredRequest(accessToken, transactionId)
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.transaction_id").value(transactionId))
            .andExpect(jsonPath("$.interval").value(300))
            .andExpect(jsonPath("$.credentials").doesNotExist());
        val walletKey = new ECKeyGenerator(Curve.P_256).algorithm(JWEAlgorithm.ECDH_ES).generate();
        postCredentialRequest(mockMvc, accessToken, MediaType.APPLICATION_JSON, MAPPER.writeValueAsString(Map.of("transaction_id", transactionId,
                "credential_response_encryption", Map.of("jwk", walletKey.toPublicJWK().toJSONObject(), "enc", "A128GCM"))), DEFERRED_URL)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_ENCRYPTION_PARAMETERS));
        val otherService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(otherService);
        val otherAccessToken = obtainAccessToken(mockMvc, otherService.getClientId(),
            otherService.getClientSecrets().getFirst().getValue(), "DeferredCredential");
        for (val request : List.of(Map.of("transaction_id", transactionId), Map.of("transaction_id", "TST-unknown"), Map.of())) {
            postCredentialRequest(mockMvc, otherAccessToken, MediaType.APPLICATION_JSON, MAPPER.writeValueAsString(request), DEFERRED_URL)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_TRANSACTION_ID));
        }

        assertEquals(400, endpoint.decide(transactionId, "PENDING").getStatus());
        assertEquals(404, endpoint.decide("TST-unknown", "APPROVED").getStatus());
        assertEquals(200, endpoint.decide(transactionId, "approved").getStatus());
        val response = postDeferredRequest(accessToken, transactionId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transaction_id").doesNotExist())
            .andExpect(jsonPath("$.notification_id").exists())
            .andReturn().getResponse().getContentAsString();
        val credential = JsonPath.read(response, "$.credentials[0].credential").toString();
        assertNotNull(SignedJWT.parse(StringUtils.substringBefore(credential, "~")).getJWTClaimsSet().getSubject());
        postDeferredRequest(accessToken, transactionId)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_TRANSACTION_ID));

        val deniedTransactionId = requestDeferredCredential(accessToken);
        assertEquals(200, endpoint.decide(deniedTransactionId, "DENIED").getStatus());
        postDeferredRequest(accessToken, deniedTransactionId)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_CREDENTIAL_REQUEST_DENIED));
        postDeferredRequest(accessToken, deniedTransactionId)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_TRANSACTION_ID));
    }

    @Test
    void verifyDeferredTransactionSurvivesFailedDelivery() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        val accessToken = obtainAccessToken(mockMvc, registeredService.getClientId(),
            registeredService.getClientSecrets().getFirst().getValue(), "DeferredReviewCredential");
        val transactionId = requestDeferredCredential(accessToken, "DeferredReviewCredential");
        val endpoint = oidcVerifiableCredentialDeferredIssuanceEndpoint.getObject();
        assertEquals(200, endpoint.decide(transactionId, "APPROVED").getStatus());
        postDeferredRequest(accessToken, transactionId)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST));
        assertTrue(endpoint.getTransactions(null).stream().anyMatch(transaction -> transaction.transactionId().equals(transactionId)
            && "APPROVED".equals(transaction.status().name())));
    }

    private String requestDeferredCredential(final String accessToken) throws Exception {
        return requestDeferredCredential(accessToken, "DeferredCredential");
    }

    private String requestDeferredCredential(final String accessToken, final String credentialConfigurationId) throws Exception {
        val request = buildCredentialRequest(fetchNonce(mockMvc), null);
        request.setCredentialConfigurationId(credentialConfigurationId);
        val response = postCredentialRequest(mockMvc, accessToken, MediaType.APPLICATION_JSON, MAPPER.writeValueAsString(request))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.credentials").doesNotExist())
            .andExpect(jsonPath("$.notification_id").doesNotExist())
            .andExpect(jsonPath("$.interval").value(300))
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.transaction_id").toString();
    }

    private ResultActions postDeferredRequest(final String accessToken, final String transactionId) throws Exception {
        return postCredentialRequest(mockMvc, accessToken, MediaType.APPLICATION_JSON,
            MAPPER.writeValueAsString(Map.of("transaction_id", transactionId)), DEFERRED_URL);
    }

    @Test
    void verifyEncryptionIsRefusedWhenNotEnabled() throws Exception {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        val accessToken = obtainAccessToken(mockMvc, registeredService.getClientId(), registeredService.getClientSecrets().getFirst().getValue());
        mockMvc.perform(get(METADATA_URL).with(withHttpRequestProcessor()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.credential_request_encryption").doesNotExist())
            .andExpect(jsonPath("$.credential_response_encryption").doesNotExist());
        postCredentialRequest(mockMvc, accessToken, APPLICATION_JWT, "eyJhbGciOiJFQ0RILUVTIn0.a.b.c.d")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST));
        val walletKey = new ECKeyGenerator(Curve.P_256).algorithm(JWEAlgorithm.ECDH_ES).generate();
        postCredentialRequest(mockMvc, accessToken, MediaType.APPLICATION_JSON,
            MAPPER.writeValueAsString(buildCredentialRequest(fetchNonce(mockMvc), walletKey, "A128GCM")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_ENCRYPTION_PARAMETERS));
    }

    /**
     * Encryption is offered but not required: the request is encrypted to a key from the issuer metadata, and the
     * response comes back encrypted to the key the wallet sent with it.
     */
    @Nested
    @ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
    @TestPropertySource(properties = {
        "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.format=DC_SD_JWT",
        "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.scope=UniversityDegree",
        "management.endpoints.web.exposure.include=oidcVcStatus",
        "management.endpoint.oidcVcStatus.access=UNRESTRICTED",
        "cas.authn.oidc.vc.issuer.encryption.enabled=true",
        "cas.authn.oidc.jwks.file-system.jwks-file=classpath:vc-encryption.jwks"
    })
    class EncryptionTests extends AbstractOidcTests {
        @Test
        void verifyEncryptedRequestAndResponse() throws Exception {
            mockMvc.perform(get(METADATA_URL).with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credential_request_encryption.jwks.keys.length()").value(2))
                .andExpect(jsonPath("$.credential_request_encryption.jwks.keys[0].alg").value("RSA-OAEP-256"))
                .andExpect(jsonPath("$.credential_request_encryption.jwks.keys[0].use").value("enc"))
                .andExpect(jsonPath("$.credential_request_encryption.jwks.keys[0].d").doesNotExist())
                .andExpect(jsonPath("$.credential_request_encryption.jwks.keys[1].alg").value("ECDH-ES"))
                .andExpect(jsonPath("$.credential_request_encryption.jwks.keys[1].d").doesNotExist())
                .andExpect(jsonPath("$.credential_request_encryption.enc_values_supported[1]").value("A256GCM"))
                .andExpect(jsonPath("$.credential_request_encryption.encryption_required").value(false))
                .andExpect(jsonPath("$.credential_request_encryption.zip_values_supported").doesNotExist())
                .andExpect(jsonPath("$.credential_response_encryption.alg_values_supported[0]").value("ECDH-ES"))
                .andExpect(jsonPath("$.credential_response_encryption.alg_values_supported[1]").value("RSA-OAEP-256"))
                .andExpect(jsonPath("$.credential_response_encryption.encryption_required").value(false));

            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            servicesManager.save(registeredService);
            val accessToken = obtainAccessToken(mockMvc, registeredService.getClientId(), registeredService.getClientSecrets().getFirst().getValue());
            for (val issuerKey : fetchRequestEncryptionKeys(mockMvc)) {
                val walletKey = generateWalletKey(issuerKey);
                val request = buildCredentialRequest(fetchNonce(mockMvc), walletKey, "A256GCM");
                val response = postCredentialRequest(mockMvc, accessToken, APPLICATION_JWT, encryptRequest(request, issuerKey, EncryptionMethod.A128GCM))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(APPLICATION_JWT))
                    .andReturn().getResponse().getContentAsString();
                val jwe = JWEObject.parse(response);
                assertEquals(walletKey.getKeyID(), jwe.getHeader().getKeyID());
                assertEquals(walletKey.getAlgorithm().getName(), jwe.getHeader().getAlgorithm().getName());
                assertEquals(EncryptionMethod.A256GCM, jwe.getHeader().getEncryptionMethod());
                jwe.decrypt(walletKey instanceof final ECKey ecKey ? new ECDHDecrypter(ecKey) : new RSADecrypter((RSAKey) walletKey));
                val payload = jwe.getPayload().toString();
                assertTrue(JsonPath.read(payload, "$.credentials[0].credential").toString().contains("~"));
                assertNotNull(JsonPath.read(payload, "$.notification_id"));
            }
        }

        @Test
        void verifyEncryptionParametersAreChecked() throws Exception {
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            servicesManager.save(registeredService);
            val accessToken = obtainAccessToken(mockMvc, registeredService.getClientId(), registeredService.getClientSecrets().getFirst().getValue());
            val issuerKey = fetchRequestEncryptionKeys(mockMvc).getLast();
            val nonce = fetchNonce(mockMvc);
            val walletKey = new ECKeyGenerator(Curve.P_256).algorithm(JWEAlgorithm.ECDH_ES).generate();

            postCredentialRequest(mockMvc, accessToken, MediaType.APPLICATION_JSON,
                MAPPER.writeValueAsString(buildCredentialRequest(nonce, walletKey, "A128GCM")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_ENCRYPTION_PARAMETERS));

            val invalidRequests = List.of(
                buildCredentialRequest(nonce, new ECKeyGenerator(Curve.P_256).generate(), "A128GCM"),
                buildCredentialRequest(nonce, walletKey, "A128CBC-HS256"),
                buildCredentialRequest(nonce, new RSAKeyGenerator(2048).algorithm(JWEAlgorithm.ECDH_ES).generate(), "A128GCM"),
                buildCredentialRequest(nonce, walletKey, "A128GCM"));
            invalidRequests.getLast().getCredentialResponseEncryption().setZip("DEF");
            for (val request : invalidRequests) {
                postCredentialRequest(mockMvc, accessToken, APPLICATION_JWT, encryptRequest(request, issuerKey, EncryptionMethod.A128GCM))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_ENCRYPTION_PARAMETERS));
            }

            val unknownKey = new ECKey.Builder((ECKey) issuerKey).keyID("unknown").build();
            for (val body : List.of(encryptRequest(buildCredentialRequest(nonce, null), unknownKey, EncryptionMethod.A128GCM),
                encryptRequest(buildCredentialRequest(nonce, null), issuerKey, EncryptionMethod.A128CBC_HS256),
                "not-a-jwe")) {
                postCredentialRequest(mockMvc, accessToken, APPLICATION_JWT, body)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST));
            }

            postCredentialRequest(mockMvc, accessToken, APPLICATION_JWT,
                    encryptRequest(buildCredentialRequest(nonce, null), issuerKey, EncryptionMethod.A256GCM))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.credentials[0].credential").exists());
        }
    }

    @Nested
    @ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
    @TestPropertySource(properties = {
        "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.format=DC_SD_JWT",
        "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.scope=UniversityDegree",
        "management.endpoints.web.exposure.include=oidcVcStatus",
        "management.endpoint.oidcVcStatus.access=UNRESTRICTED",
        "cas.authn.oidc.vc.issuer.encryption.enabled=true",
        "cas.authn.oidc.vc.issuer.encryption.request-encryption-required=true",
        "cas.authn.oidc.vc.issuer.encryption.response-encryption-required=true",
        "cas.authn.oidc.jwks.file-system.jwks-file=classpath:vc-encryption.jwks"
    })
    class EncryptionRequiredTests extends AbstractOidcTests {
        @Test
        void verifyEncryptionIsRequired() throws Exception {
            mockMvc.perform(get(METADATA_URL).with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credential_request_encryption.encryption_required").value(true))
                .andExpect(jsonPath("$.credential_response_encryption.encryption_required").value(true));
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            servicesManager.save(registeredService);
            val accessToken = obtainAccessToken(mockMvc, registeredService.getClientId(), registeredService.getClientSecrets().getFirst().getValue());
            val request = buildCredentialRequest(fetchNonce(mockMvc), null);
            postCredentialRequest(mockMvc, accessToken, MediaType.APPLICATION_JSON, MAPPER.writeValueAsString(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST));
            postCredentialRequest(mockMvc, accessToken, APPLICATION_JWT,
                    encryptRequest(request, fetchRequestEncryptionKeys(mockMvc).getFirst(), EncryptionMethod.A128GCM))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_ENCRYPTION_PARAMETERS));
        }
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
                "casuser", List.of("UniversityDegreeCredential", "DeferredCredential"));
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

            credentialRequest.setCredentialConfigurationId("DeferredCredential");
            credentialRequest.setProofs(buildProofs(buildProofJwt(nonce)));
            mockMvc.perform(post(CREDENTIAL_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .content(MAPPER.writeValueAsString(credentialRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credentials[0].credential").exists())
                .andExpect(jsonPath("$.transaction_id").doesNotExist());

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
                                                           final String principal, final List<String> credentialConfigurationIds)
            throws Exception {
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
                                                               final String principal, final List<String> credentialConfigurationIds)
            throws Exception {
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
