package org.apereo.cas.oidc.web.controllers.authorize;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.authn.OidcClientAttestationAuthenticator;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequest;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.util.CollectionUtils;
import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.oauth2.sdk.dpop.DefaultDPoPProofFactory;
import com.nimbusds.openid.connect.sdk.Nonce;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcPushedAuthorizeEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("OIDCWeb")
@TestPropertySource(properties = {
    "cas.authn.oidc.discovery.require-pushed-authorization-requests=true",
    "cas.authn.oidc.client-attestation.trust-anchors=classpath:client-attestation-root.pem"
})
class OidcPushedAuthorizeEndpointControllerTests extends AbstractOidcTests {

    @Test
    void verifyGetOperationFails() throws Exception {
        val id = UUID.randomUUID().toString();
        val service = getOidcRegisteredService(id);
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);
        mockMvc.perform(get("/cas/oidc/" + OidcConstants.PUSHED_AUTHORIZE_URL)
                .param(OAuth20Constants.CLIENT_ID, id)
                .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org/")
                .param(OAuth20Constants.CLIENT_SECRET, service.getClientSecrets().getFirst().getValue())
                .param(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase(Locale.ENGLISH))
                .with(withHttpRequestProcessor())
            )
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void verifyPostWithoutRequiredParams() throws Exception {
        mockMvc.perform(post("/cas/oidc/" + OidcConstants.PUSHED_AUTHORIZE_URL)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isForbidden());
    }

    @Test
    void verifyPostOperation() throws Exception {
        val id = UUID.randomUUID().toString();
        val service = getOidcRegisteredService(id);
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);

        mockMvc.perform(post("/cas/oidc/" + OidcConstants.PUSHED_AUTHORIZE_URL)
                .param(OAuth20Constants.CLIENT_ID, id)
                .param(OAuth20Constants.CLIENT_SECRET, service.getClientSecrets().getFirst().getValue())
                .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org/")
                .param(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase(Locale.ENGLISH))
                .with(withHttpRequestProcessor()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.request_uri").exists())
            .andExpect(jsonPath("$.expires_in").exists());
    }

    @Test
    void verifyPostOperationInvalidRedirectUri() throws Exception {
        val id = UUID.randomUUID().toString();
        val service = getOidcRegisteredService(id, "https://valid.example.org");
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);

        mockMvc.perform(post("/cas/oidc/" + OidcConstants.PUSHED_AUTHORIZE_URL)
                .param(OAuth20Constants.CLIENT_ID, id)
                .param(OAuth20Constants.CLIENT_SECRET, service.getClientSecrets().getFirst().getValue())
                .param(OAuth20Constants.REDIRECT_URI, "https://invalid.example.org/")
                .param(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase(Locale.ENGLISH))
                .with(withHttpRequestProcessor()))
            .andExpect(status().isForbidden());
    }

    @Test
    void verifyPostWithInvalidIssuer() throws Exception {
        val id = UUID.randomUUID().toString();
        val service = getOidcRegisteredService(id);
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);

        mockMvc.perform(post("/cas/oidc/" + OidcConstants.PUSHED_AUTHORIZE_URL)
                .param(OAuth20Constants.CLIENT_ID, id)
                .param(OAuth20Constants.CLIENT_SECRET, service.getClientSecrets().getFirst().getValue())
                .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org/")
                .param(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase(Locale.ENGLISH))
                .with(withHttpRequestProcessor())
                .with(request -> {
                    request.setServerName("invalid.example.org");
                    return request;
                }))
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyPostWithClientAttestation() throws Exception {
        val id = UUID.randomUUID().toString();
        val service = getOidcRegisteredService(id);
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);

        val instanceKey = new ECKeyGenerator(Curve.P_256).generate();
        val attestation = buildClientAttestation(id, instanceKey, false);
        val issuer = oidcServerDiscoverySettings.getIssuer();
        val proof = buildClientAttestationProof(instanceKey, issuer);
        performPushedAuthorizationRequest(id, attestation, proof)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.request_uri").exists());
        performPushedAuthorizationRequest(id, attestation, proof).andExpect(status().isUnauthorized());

        performPushedAuthorizationRequest(id, attestation,
            buildClientAttestationProof(new ECKeyGenerator(Curve.P_256).generate(), issuer)).andExpect(status().isUnauthorized());
        performPushedAuthorizationRequest(id, attestation,
            buildClientAttestationProof(instanceKey, "https://other.example.org")).andExpect(status().isUnauthorized());
        performPushedAuthorizationRequest(id, buildClientAttestation(id, instanceKey, true),
            buildClientAttestationProof(instanceKey, issuer)).andExpect(status().isUnauthorized());
        performPushedAuthorizationRequest(id, buildClientAttestation(UUID.randomUUID().toString(), instanceKey, false),
            buildClientAttestationProof(instanceKey, issuer)).andExpect(status().isUnauthorized());

        mockMvc.perform(post("/cas/oidc/" + OidcConstants.PUSHED_AUTHORIZE_URL)
                .param(OAuth20Constants.CLIENT_ID, id)
                .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org/")
                .param(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase(Locale.ENGLISH))
                .header(OidcClientAttestationAuthenticator.HEADER_CLIENT_ATTESTATION, attestation, attestation)
                .header(OidcClientAttestationAuthenticator.HEADER_CLIENT_ATTESTATION_POP, buildClientAttestationProof(instanceKey, issuer))
                .with(withHttpRequestProcessor()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyPostWithClientAttestationInDPoPCombinedMode() throws Exception {
        val id = UUID.randomUUID().toString();
        val service = getOidcRegisteredService(id);
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);

        val instanceKey = new ECKeyGenerator(Curve.P_256).generate();
        val attestation = buildClientAttestation(id, instanceKey, false);
        val proof = buildDPoPProof(instanceKey);
        performPushedAuthorizationRequestWithDPoP(id, attestation, proof)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.request_uri").exists());
        performPushedAuthorizationRequestWithDPoP(id, attestation, proof).andExpect(status().isUnauthorized());
        performPushedAuthorizationRequestWithDPoP(id, attestation, buildDPoPProof(new ECKeyGenerator(Curve.P_256).generate()))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/cas/oidc/" + OidcConstants.CLIENT_ATTESTATION_CHALLENGE_URL).with(withHttpRequestProcessor()))
            .andExpect(status().isNotFound());
    }

    @Test
    void verifyPostBindsAuthorizationCodeToDPoPKey() throws Throwable {
        val id = UUID.randomUUID().toString();
        val service = getOidcRegisteredService(id);
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);
        val secret = service.getClientSecrets().getFirst().getValue();
        val key = new ECKeyGenerator(Curve.P_256).generate();
        val thumbprint = key.computeThumbprint().toString();

        assertNull(findBoundKey(performKeyBoundPushedAuthorizationRequest(id, secret, null)
            .andExpect(status().isCreated())));
        assertEquals(thumbprint, findBoundKey(performKeyBoundPushedAuthorizationRequest(id, secret, thumbprint)
            .andExpect(status().isCreated())));
        assertEquals(thumbprint, findBoundKey(performKeyBoundPushedAuthorizationRequest(id, secret, null, buildDPoPProof(key))
            .andExpect(status().isCreated())));
        assertEquals(thumbprint, findBoundKey(performKeyBoundPushedAuthorizationRequest(id, secret, thumbprint, buildDPoPProof(key))
            .andExpect(status().isCreated())));

        val otherThumbprint = new ECKeyGenerator(Curve.P_256).generate().computeThumbprint().toString();
        val proofForAnotherEndpoint = new DefaultDPoPProofFactory(key, JWSAlgorithm.ES256)
            .createDPoPJWT("POST", new URI("https://sso.example.org/cas/oidc/" + OidcConstants.TOKEN_URL)).serialize();
        for (val actions : List.of(
            performKeyBoundPushedAuthorizationRequest(id, secret, otherThumbprint, buildDPoPProof(key)),
            performKeyBoundPushedAuthorizationRequest(id, secret, null, proofForAnotherEndpoint),
            performKeyBoundPushedAuthorizationRequest(id, secret, null, buildDPoPProof(key), buildDPoPProof(key)))) {
            actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_DPOP_PROOF));
        }
    }

    private ResultActions performKeyBoundPushedAuthorizationRequest(final String clientId, final String clientSecret,
                                                                    @Nullable final String thumbprint,
                                                                    final String... dpopProofs)
            throws Exception {
        val request = post("/cas/oidc/" + OidcConstants.PUSHED_AUTHORIZE_URL)
            .param(OAuth20Constants.CLIENT_ID, clientId)
            .param(OAuth20Constants.CLIENT_SECRET, clientSecret)
            .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org/")
            .param(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase(Locale.ENGLISH))
            .with(withHttpRequestProcessor());
        if (thumbprint != null) {
            request.param(OAuth20Constants.DPOP_JKT, thumbprint);
        }
        for (val proof : dpopProofs) {
            request.header(OAuth20Constants.DPOP, proof);
        }
        return mockMvc.perform(request);
    }

    private @Nullable String findBoundKey(final ResultActions actions) throws Exception {
        val requestUri = JsonPath.read(actions.andReturn().getResponse().getContentAsString(), "$.request_uri").toString();
        val request = ticketRegistry.getTicket(requestUri, OidcPushedAuthorizationRequest.class);
        return CollectionUtils.firstElement(request.getAuthentication().getAttributes().get(OAuth20Constants.DPOP_JKT))
            .map(Object::toString)
            .orElse(null);
    }

    private ResultActions performPushedAuthorizationRequest(final String clientId, final String attestation,
                                                            final String proof)
            throws Exception {
        return mockMvc.perform(post("/cas/oidc/" + OidcConstants.PUSHED_AUTHORIZE_URL)
            .param(OAuth20Constants.CLIENT_ID, clientId)
            .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org/")
            .param(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase(Locale.ENGLISH))
            .header(OidcClientAttestationAuthenticator.HEADER_CLIENT_ATTESTATION, attestation)
            .header(OidcClientAttestationAuthenticator.HEADER_CLIENT_ATTESTATION_POP, proof)
            .with(withHttpRequestProcessor()));
    }

    private ResultActions performPushedAuthorizationRequestWithDPoP(final String clientId, final String attestation,
                                                                    final String dpopProof)
            throws Exception {
        return mockMvc.perform(post("/cas/oidc/" + OidcConstants.PUSHED_AUTHORIZE_URL)
            .param(OAuth20Constants.CLIENT_ID, clientId)
            .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org/")
            .param(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase(Locale.ENGLISH))
            .header(OidcClientAttestationAuthenticator.HEADER_CLIENT_ATTESTATION, attestation)
            .header(OAuth20Constants.DPOP, dpopProof)
            .with(withHttpRequestProcessor()));
    }

    private static String buildDPoPProof(final ECKey key) throws Exception {
        return buildDPoPProof(key, null);
    }

    private static String buildDPoPProof(final ECKey key, @Nullable final String nonce) throws Exception {
        return new DefaultDPoPProofFactory(key, JWSAlgorithm.ES256)
            .createDPoPJWT("POST", new URI("https://sso.example.org/cas/oidc/" + OidcConstants.PUSHED_AUTHORIZE_URL),
                nonce == null ? null : new Nonce(nonce))
            .serialize();
    }

    /**
     * Once challenges are turned on, the proof of possession must carry one handed out by CAS; once DPoP nonces are turned on,
     * so must the DPoP proof of the DPoP combined mode.
     */
    @Nested
    @TestPropertySource(properties = {
        "cas.authn.oidc.discovery.require-pushed-authorization-requests=true",
        "cas.authn.oidc.client-attestation.trust-anchors=classpath:client-attestation-root.pem",
        "cas.authn.oidc.client-attestation.challenge.enabled=true",
        "cas.authn.oidc.dpop.nonce.enabled=true"
    })
    class ChallengeTests extends AbstractOidcTests {
        @Test
        void verifyPostWithClientAttestationChallenge() throws Exception {
            val id = UUID.randomUUID().toString();
            val service = getOidcRegisteredService(id);
            service.setBypassApprovalPrompt(true);
            servicesManager.save(service);
            assertTrue(oidcServerDiscoverySettings.getChallengeEndpoint().endsWith('/' + OidcConstants.CLIENT_ATTESTATION_CHALLENGE_URL));

            val challengeResponse = mockMvc.perform(post("/cas/oidc/" + OidcConstants.CLIENT_ATTESTATION_CHALLENGE_URL)
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andReturn().getResponse().getContentAsString();
            val challenge = JsonPath.read(challengeResponse, "$.attestation_challenge").toString();

            val instanceKey = new ECKeyGenerator(Curve.P_256).generate();
            val attestation = buildClientAttestation(id, instanceKey, false);
            val issuer = oidcServerDiscoverySettings.getIssuer();
            for (val missing : Arrays.asList(null, "unknown")) {
                performPushedAuthorizationRequest(id, attestation, buildClientAttestationProof(instanceKey, issuer, missing))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(OidcConstants.USE_ATTESTATION_CHALLENGE))
                    .andExpect(header().exists(OidcConstants.HEADER_CLIENT_ATTESTATION_CHALLENGE));
            }
            val freshChallenge = performPushedAuthorizationRequest(id, attestation, buildClientAttestationProof(instanceKey, issuer, null))
                .andReturn().getResponse().getHeader(OidcConstants.HEADER_CLIENT_ATTESTATION_CHALLENGE);
            for (val provided : List.of(challenge, challenge, Objects.requireNonNull(freshChallenge))) {
                performPushedAuthorizationRequest(id, attestation, buildClientAttestationProof(instanceKey, issuer, provided))
                    .andExpect(status().isCreated());
            }
        }

        @Test
        void verifyPostInDPoPCombinedModeWithNonce() throws Exception {
            val id = UUID.randomUUID().toString();
            val service = getOidcRegisteredService(id);
            service.setBypassApprovalPrompt(true);
            servicesManager.save(service);

            val nonce = mockMvc.perform(post("/cas/oidc/" + OidcConstants.CLIENT_ATTESTATION_CHALLENGE_URL)
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attestation_challenge").exists())
                .andReturn().getResponse().getHeader(OAuth20Constants.DPOP_NONCE);
            assertNotNull(nonce);

            val instanceKey = new ECKeyGenerator(Curve.P_256).generate();
            val attestation = buildClientAttestation(id, instanceKey, false);
            val freshNonce = performPushedAuthorizationRequestWithDPoP(id, attestation, buildDPoPProof(instanceKey, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.USE_DPOP_NONCE))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(header().doesNotExist(OidcConstants.HEADER_CLIENT_ATTESTATION_CHALLENGE))
                .andReturn().getResponse().getHeader(OAuth20Constants.DPOP_NONCE);
            assertNotNull(freshNonce);
            performPushedAuthorizationRequestWithDPoP(id, attestation, buildDPoPProof(instanceKey, "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.USE_DPOP_NONCE))
                .andExpect(header().exists(OAuth20Constants.DPOP_NONCE));
            for (val provided : List.of(nonce, nonce, freshNonce)) {
                performPushedAuthorizationRequestWithDPoP(id, attestation, buildDPoPProof(instanceKey, provided))
                    .andExpect(status().isCreated());
            }
        }

        @Test
        void verifyPostBindsAuthorizationCodeToDPoPKeyWithNonce() throws Throwable {
            val id = UUID.randomUUID().toString();
            val service = getOidcRegisteredService(id);
            service.setBypassApprovalPrompt(true);
            servicesManager.save(service);
            val secret = service.getClientSecrets().getFirst().getValue();
            val key = new ECKeyGenerator(Curve.P_256).generate();

            val nonce = performKeyBoundPushedAuthorizationRequest(id, secret, null, buildDPoPProof(key))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.USE_DPOP_NONCE))
                .andReturn().getResponse().getHeader(OAuth20Constants.DPOP_NONCE);
            assertNotNull(nonce);
            assertEquals(key.computeThumbprint().toString(),
                findBoundKey(performKeyBoundPushedAuthorizationRequest(id, secret, null, buildDPoPProof(key, nonce))
                    .andExpect(status().isCreated())));
        }
    }
}
