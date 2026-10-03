package org.apereo.cas.vc.presentation;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialPresentationRequestEndpointController.OidcVerifiableCredentialPresentationRequest;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialPresentationRequestEndpointController.OidcVerifiableCredentialPresentationRequest.ClaimRequest;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialPresentationRequestEndpointController.OidcVerifiableCredentialPresentationRequest.CredentialRequest;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialPresentationRequestEndpointController.OidcVerifiableCredentialPresentationResponse;
import com.google.common.base.Splitter;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.util.X509CertUtils;
import com.nimbusds.jwt.SignedJWT;
import lombok.val;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcVerifiableCredentialPresentationRequestEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("OIDCWeb")
class OidcVerifiableCredentialPresentationRequestEndpointControllerTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static final String PRESENTATION_REQUEST_ENDPOINT_URL =
        "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_PRESENTATION_REQUEST_URL;

    private static OidcVerifiableCredentialPresentationRequest buildPresentationRequest() {
        val claim = new ClaimRequest();
        claim.setPath(List.of("given_name"));
        claim.setRequired(true);

        val credential = new CredentialRequest();
        credential.setId("university-degree");
        credential.setFormat("dc+sd-jwt");
        credential.setVctValues(List.of("UniversityDegreeCredential"));
        credential.setClaims(List.of(claim));

        val request = new OidcVerifiableCredentialPresentationRequest();
        request.setCredentials(List.of(credential));
        return request;
    }

    private static Map<String, String> parseQueryParameters(final URI uri) {
        val parameters = new LinkedHashMap<String, String>();
        for (val pair : Splitter.on('&').split(uri.getRawQuery())) {
            val index = pair.indexOf('=');
            parameters.put(URLDecoder.decode(pair.substring(0, index), StandardCharsets.UTF_8),
                URLDecoder.decode(pair.substring(index + 1), StandardCharsets.UTF_8));
        }
        return parameters;
    }

    @ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
    @TestPropertySource(properties = {
        "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.format=DC_SD_JWT",
        "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.scope=UniversityDegree"
    })
    abstract static class BaseTests extends AbstractOidcTests {
        protected ResultActions performPresentationRequest(final OidcVerifiableCredentialPresentationRequest request) throws Exception {
            return mockMvc.perform(post(PRESENTATION_REQUEST_ENDPOINT_URL)
                .with(withHttpRequestProcessor())
                .param(OAuth20Constants.CLIENT_ID, getOidcRegisteredService().getClientId())
                .param(OAuth20Constants.CLIENT_SECRET, getOidcRegisteredService().getClientSecrets().getFirst().getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)));
        }

        protected OidcVerifiableCredentialPresentationResponse createPresentationRequest() throws Exception {
            return createPresentationRequest(buildPresentationRequest());
        }

        protected OidcVerifiableCredentialPresentationResponse createPresentationRequest(
            final OidcVerifiableCredentialPresentationRequest request) throws Exception {
            val responseBody = performPresentationRequest(request)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
            return MAPPER.readValue(responseBody, OidcVerifiableCredentialPresentationResponse.class);
        }

        protected String getResponseUri() {
            return casProperties.getAuthn().getOidc().getCore().getIssuer()
                + '/' + OidcConstants.VC_PRESENTATION_RESPONSE_URL;
        }

        protected TransientSessionTicket createPresentationTransaction(final String nonce, final String state) throws Throwable {
            val factory = (TransientSessionTicketFactory) defaultTicketFactory.get(TransientSessionTicket.class);
            val ticket = factory.create(CollectionUtils.wrap(
                "nonce", nonce,
                "state", state,
                "credentials", buildPresentationRequest().getCredentials()));
            ticketRegistry.addTicket(ticket);
            return ticket;
        }
    }

    /**
     * OpenID4VP 1.0 forbids signing a request made under the {@code redirect_uri} client
     * identifier prefix, and a request URI response must carry a signed request object.
     * The request is therefore passed to the wallet by value and no request URI is offered.
     */
    @Nested
    class UnsignedRequestTests extends BaseTests {

        @Test
        void verifyPresentationRequestCreationRejectsUnauthenticatedClients() throws Throwable {
            mockMvc.perform(post(PRESENTATION_REQUEST_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(MAPPER.writeValueAsString(buildPresentationRequest())))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void verifyEmptyPresentationRequestIsRejected() throws Exception {
            mockMvc.perform(post(PRESENTATION_REQUEST_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .param(OAuth20Constants.CLIENT_ID, getOidcRegisteredService().getClientId())
                    .param(OAuth20Constants.CLIENT_SECRET, getOidcRegisteredService().getClientSecrets().getFirst().getValue())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"credentials\":[]}"))
                .andExpect(status().isBadRequest());
        }

        @Test
        void verifyRequestMayAskForAnEncryptedResponse() throws Throwable {
            val encryptedRequest = buildPresentationRequest();
            encryptedRequest.setResponseMode("direct_post.jwt");
            val encrypted = parseQueryParameters(URI.create(createPresentationRequest(encryptedRequest).getAuthorizationRequest()));
            assertEquals("direct_post.jwt", encrypted.get(OAuth20Constants.RESPONSE_MODE));
            assertTrue(encrypted.get("client_metadata").contains("jwks"));

            val unknownRequest = buildPresentationRequest();
            unknownRequest.setResponseMode("fragment");
            performPresentationRequest(unknownRequest).andExpect(status().isBadRequest());
        }

        @Test
        void verifyAuthorizationRequestCarriesEveryParameterByValue() throws Throwable {
            val response = createPresentationRequest();
            assertTrue(response.getRequestId().startsWith(TransientSessionTicket.PREFIX));
            assertNull(response.getRequestUri(), "No request URI may be offered for an unsigned request");

            val authorizationRequest = URI.create(response.getAuthorizationRequest());
            assertEquals("openid4vp", authorizationRequest.getScheme());
            val parameters = parseQueryParameters(authorizationRequest);
            assertFalse(parameters.containsKey(OidcConstants.REQUEST_URI));
            assertEquals("redirect_uri:" + getResponseUri(), parameters.get(OAuth20Constants.CLIENT_ID));
            assertEquals("vp_token", parameters.get(OAuth20Constants.RESPONSE_TYPE));
            assertEquals("direct_post", parameters.get(OAuth20Constants.RESPONSE_MODE));
            assertEquals(getResponseUri(), parameters.get("response_uri"));
            assertEquals(response.getRequestId(), parameters.get(OAuth20Constants.STATE));
            assertFalse(parameters.get("client_metadata").contains("jwks"));

            val ticket = ticketRegistry.getTicket(response.getRequestId(), TransientSessionTicket.class);
            assertNotNull(ticket);
            assertEquals(ticket.getPropertyAsString("nonce"), parameters.get(OAuth20Constants.NONCE));
            assertEquals(ticket.getExpirationPolicy().getTimeToLive(), response.getExpiresIn());

            val dcqlQuery = MAPPER.readValue(parameters.get("dcql_query"), Map.class);
            val credentials = assertInstanceOf(List.class, dcqlQuery.get("credentials"));
            assertEquals(1, credentials.size());
            val credential = assertInstanceOf(Map.class, credentials.getFirst());
            assertEquals("university-degree", credential.get("id"));
            assertEquals("dc+sd-jwt", credential.get("format"));
            assertEquals(List.of("UniversityDegreeCredential"),
                assertInstanceOf(Map.class, credential.get("meta")).get("vct_values"));
            val claims = assertInstanceOf(List.class, credential.get("claims"));
            assertEquals(List.of("given_name"), assertInstanceOf(Map.class, claims.getFirst()).get("path"));
            assertNull(assertInstanceOf(Map.class, claims.getFirst()).get("id"));
            assertNull(credential.get("claim_sets"));

            val clientMetadata = MAPPER.readValue(parameters.get("client_metadata"), Map.class);
            assertEquals("Apereo CAS", clientMetadata.get("client_name"));
            val vpFormats = assertInstanceOf(Map.class, clientMetadata.get("vp_formats_supported"));
            val sdJwtFormat = assertInstanceOf(Map.class, vpFormats.get("dc+sd-jwt"));
            assertNull(sdJwtFormat.get("alg_values"));
            assertEquals(List.of("ES256", "RS256"), sdJwtFormat.get("sd-jwt_alg_values"));
            assertEquals(OidcVerifiableCredentialPresentationResponseEndpointController.KEY_BINDING_ALGORITHMS_SUPPORTED,
                sdJwtFormat.get("kb-jwt_alg_values"));

            assertEquals(getOidcRegisteredService().getClientId(),
                ticket.getPropertyAsString(OidcVerifiableCredentialPresentationResponseEndpointController.PROPERTY_CLIENT_ID));
            assertNull(ticket.getPropertyAsString(OidcVerifiableCredentialPresentationResponseEndpointController.PROPERTY_REDIRECT_URI));
        }

        @Test
        void verifyOptionalClaimsAreRequestedThroughClaimSets() {
            val required = new ClaimRequest();
            required.setPath(List.of("given_name"));
            val optional = new ClaimRequest();
            optional.setPath(List.of("email"));
            optional.setRequired(false);

            val credential = new CredentialRequest();
            credential.setId("university-degree");
            credential.setFormat("dc+sd-jwt");
            credential.setClaims(List.of(required, optional));
            val query = OidcVerifiableCredentialPresentationRequestEndpointController.toCredentialQuery(credential);
            assertEquals(List.of("claim-0", "claim-1"), query.getClaims().stream().map(claim -> claim.getId()).toList());
            assertEquals(List.of(List.of("claim-0", "claim-1"), List.of("claim-0")), query.getClaimSets());

            required.setRequired(false);
            val allOptional = OidcVerifiableCredentialPresentationRequestEndpointController.toCredentialQuery(credential);
            assertEquals(List.of(List.of("claim-0", "claim-1"), List.of("claim-0"), List.of("claim-1")), allOptional.getClaimSets());

            credential.setClaims(null);
            val noClaims = OidcVerifiableCredentialPresentationRequestEndpointController.toCredentialQuery(credential);
            assertTrue(noClaims.getClaims().isEmpty());
            assertTrue(noClaims.getClaimSets().isEmpty());
        }

        @Test
        void verifyRedirectUriMustBeRegisteredForTheClient() throws Throwable {
            val request = buildPresentationRequest();
            request.setRedirectUri("https://attacker.example.net/callback");
            mockMvc.perform(post(PRESENTATION_REQUEST_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .param(OAuth20Constants.CLIENT_ID, getOidcRegisteredService().getClientId())
                    .param(OAuth20Constants.CLIENT_SECRET, getOidcRegisteredService().getClientSecrets().getFirst().getValue())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            request.setRedirectUri("https://oauth.example.org/callback");
            val responseBody = mockMvc.perform(post(PRESENTATION_REQUEST_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .param(OAuth20Constants.CLIENT_ID, getOidcRegisteredService().getClientId())
                    .param(OAuth20Constants.CLIENT_SECRET, getOidcRegisteredService().getClientSecrets().getFirst().getValue())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
            val response = MAPPER.readValue(responseBody, OidcVerifiableCredentialPresentationResponse.class);
            val ticket = ticketRegistry.getTicket(response.getRequestId(), TransientSessionTicket.class);
            assertEquals("https://oauth.example.org/callback",
                ticket.getPropertyAsString(OidcVerifiableCredentialPresentationResponseEndpointController.PROPERTY_REDIRECT_URI));
        }

        @Test
        void verifyRequestObjectIsNotServedByReference() throws Throwable {
            val ticket = createPresentationTransaction(UUID.randomUUID().toString(), UUID.randomUUID().toString());
            mockMvc.perform(get(PRESENTATION_REQUEST_ENDPOINT_URL + '/' + ticket.getId())
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isNotFound());
        }
    }

    /**
     * HAIP 1.0 section 5: the wallet encrypts its response ({@code direct_post.jwt}) to an ephemeral key generated for
     * each request and published in its client metadata.
     */
    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.vc.presentation.response-mode=DIRECT_POST_JWT")
    class EncryptedResponseTests extends BaseTests {
        @Test
        void verifyEphemeralResponseEncryptionKey() throws Throwable {
            val response = createPresentationRequest();
            val parameters = parseQueryParameters(URI.create(response.getAuthorizationRequest()));
            assertEquals("direct_post.jwt", parameters.get(OAuth20Constants.RESPONSE_MODE));
            val clientMetadata = MAPPER.readValue(parameters.get("client_metadata"), Map.class);
            assertEquals(List.of("A128GCM", "A256GCM"), clientMetadata.get("encrypted_response_enc_values_supported"));
            val keys = assertInstanceOf(List.class, assertInstanceOf(Map.class, clientMetadata.get("jwks")).get("keys"));
            assertEquals(1, keys.size());
            val key = assertInstanceOf(Map.class, keys.getFirst());
            assertEquals("EC", key.get("kty"));
            assertEquals("P-256", key.get("crv"));
            assertEquals("ECDH-ES", key.get("alg"));
            assertEquals("enc", key.get("use"));
            assertEquals(response.getRequestId(), key.get("kid"));
            assertFalse(key.containsKey("d"));

            val ticket = ticketRegistry.getTicket(response.getRequestId(), TransientSessionTicket.class);
            assertNotNull(ticket);
            val privateKey = PublicJsonWebKey.Factory.newPublicJwk(Objects.requireNonNull(ticket.getPropertyAsString(
                OidcVerifiableCredentialPresentationResponseEndpointController.PROPERTY_RESPONSE_ENCRYPTION_KEY)));
            assertNotNull(privateKey.getPrivateKey());
            assertEquals(key.get("x"), privateKey.toParams(JsonWebKey.OutputControlLevel.PUBLIC_ONLY).get("x"));

            val other = parseQueryParameters(URI.create(createPresentationRequest().getAuthorizationRequest()));
            assertNotEquals(parameters.get("client_metadata"), other.get("client_metadata"));

            val plainRequest = buildPresentationRequest();
            plainRequest.setResponseMode("direct_post");
            performPresentationRequest(plainRequest).andExpect(status().isBadRequest());
        }
    }

    /**
     * With a client identifier prefix that permits signing, the request object is served by
     * reference as a signed JWT, and the deep link carries only the client identifier and the
     * request URI.
     */
    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.vc.presentation.client-identifier-prefix=X509_SAN_DNS")
    class SignedRequestTests extends BaseTests {

        @Test
        void verifyAuthorizationRequestIsPassedByReference() throws Throwable {
            val response = createPresentationRequest();
            val issuer = casProperties.getAuthn().getOidc().getCore().getIssuer();
            assertEquals(issuer + '/' + OidcConstants.VC_PRESENTATION_REQUEST_URL + '/' + response.getRequestId(),
                response.getRequestUri());

            val parameters = parseQueryParameters(URI.create(response.getAuthorizationRequest()));
            assertEquals(Set.of(OAuth20Constants.CLIENT_ID, OidcConstants.REQUEST_URI), parameters.keySet());
            assertEquals("x509_san_dns:" + URI.create(getResponseUri()).getHost(),
                parameters.get(OAuth20Constants.CLIENT_ID));
            assertEquals(response.getRequestUri(), parameters.get(OidcConstants.REQUEST_URI));
        }

        /**
         * The request object media type is set on the response rather than declared as a produces
         * condition, so a wallet with a narrow Accept header reaches the handler instead of being
         * turned away by content negotiation with a 406.
         */
        @Test
        void verifyNarrowAcceptHeaderReachesTheHandler() throws Throwable {
            val ticket = createPresentationTransaction(UUID.randomUUID().toString(), UUID.randomUUID().toString());
            mockMvc.perform(get(PRESENTATION_REQUEST_ENDPOINT_URL + '/' + ticket.getId())
                    .with(withHttpRequestProcessor())
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        }

        @Test
        void verifyRequestObjectIsRefusedWithoutCertificateChain() throws Throwable {
            val ticket = createPresentationTransaction(UUID.randomUUID().toString(), UUID.randomUUID().toString());
            val response = mockMvc.perform(get(PRESENTATION_REQUEST_ENDPOINT_URL + '/' + ticket.getId())
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
            assertTrue(response.contains("certificate chain"),
                () -> "Expected a configuration error about the signing certificate chain but got " + response);
        }
    }

    /**
     * HAIP 1.0 section 5: the request object's {@code x5c} carries the chain without its trust anchor.
     */
    @Nested
    @TestPropertySource(properties = {
        "cas.authn.oidc.vc.presentation.client-identifier-prefix=X509_SAN_DNS",
        "cas.authn.oidc.jwks.file-system.jwks-file=classpath:vc-issuer-x5c.jwks"
    })
    class SignedRequestCertificateChainTests extends BaseTests {
        @Test
        void verifyRequestObjectCertificateChainWithoutTrustAnchor() throws Throwable {
            val ticket = createPresentationTransaction(UUID.randomUUID().toString(), UUID.randomUUID().toString());
            val requestObject = SignedJWT.parse(mockMvc.perform(get(PRESENTATION_REQUEST_ENDPOINT_URL + '/' + ticket.getId())
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
            val issuerKey = assertInstanceOf(ECKey.class, JWKSet.load(
                new ClassPathResource("vc-issuer-x5c.jwks").getInputStream()).getKeyByKeyId("vc-issuer"));
            assertEquals(2, issuerKey.getParsedX509CertChain().size());

            assertEquals("oauth-authz-req+jwt", requestObject.getHeader().getType().toString());
            val chain = requestObject.getHeader().getX509CertChain();
            assertEquals(1, chain.size());
            val leaf = X509CertUtils.parse(chain.getFirst().decode());
            assertEquals(issuerKey.getParsedX509CertChain().getFirst(), leaf);
            assertTrue(requestObject.verify(new ECDSAVerifier((ECPublicKey) leaf.getPublicKey())));
        }
    }

    /**
     * OpenID4VP 1.0 section 5.9.3 and HAIP 1.0 section 5: under {@code x509_hash} the client identifier is the
     * base64url-encoded SHA-256 hash of the DER-encoded leaf certificate that signs the request object.
     */
    @Nested
    @TestPropertySource(properties = {
        "cas.authn.oidc.vc.presentation.client-identifier-prefix=X509_HASH",
        "cas.authn.oidc.jwks.file-system.jwks-file=classpath:vc-issuer-x5c.jwks"
    })
    class X509HashRequestTests extends BaseTests {
        @Test
        void verifyClientIdentifierIsTheLeafCertificateHash() throws Throwable {
            val leaf = assertInstanceOf(ECKey.class, JWKSet.load(new ClassPathResource("vc-issuer-x5c.jwks").getInputStream())
                .getKeyByKeyId("vc-issuer")).getParsedX509CertChain().getFirst();
            val clientId = "x509_hash:" + X509CertUtils.computeSHA256Thumbprint(leaf);

            val response = createPresentationRequest();
            assertNotNull(response.getRequestUri());
            val parameters = parseQueryParameters(URI.create(response.getAuthorizationRequest()));
            assertEquals(clientId, parameters.get(OAuth20Constants.CLIENT_ID));

            val requestObject = SignedJWT.parse(mockMvc.perform(get(PRESENTATION_REQUEST_ENDPOINT_URL + '/' + response.getRequestId())
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
            assertEquals(clientId, requestObject.getJWTClaimsSet().getStringClaim(OAuth20Constants.CLIENT_ID));
            assertEquals(clientId, requestObject.getJWTClaimsSet().getIssuer());
            val chain = requestObject.getHeader().getX509CertChain();
            assertEquals(1, chain.size());
            assertEquals(leaf, X509CertUtils.parse(chain.getFirst().decode()));
            assertTrue(requestObject.verify(new ECDSAVerifier((ECPublicKey) leaf.getPublicKey())));
        }
    }
}
