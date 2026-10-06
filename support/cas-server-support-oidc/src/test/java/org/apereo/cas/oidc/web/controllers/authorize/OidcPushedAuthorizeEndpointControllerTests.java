package org.apereo.cas.oidc.web.controllers.authorize;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.authn.OidcClientAttestationAuthenticator;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.oauth2.sdk.dpop.DefaultDPoPProofFactory;
import lombok.val;
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

    private ResultActions performPushedAuthorizationRequest(final String clientId, final String attestation,
                                                            final String proof) throws Exception {
        return mockMvc.perform(post("/cas/oidc/" + OidcConstants.PUSHED_AUTHORIZE_URL)
            .param(OAuth20Constants.CLIENT_ID, clientId)
            .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org/")
            .param(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase(Locale.ENGLISH))
            .header(OidcClientAttestationAuthenticator.HEADER_CLIENT_ATTESTATION, attestation)
            .header(OidcClientAttestationAuthenticator.HEADER_CLIENT_ATTESTATION_POP, proof)
            .with(withHttpRequestProcessor()));
    }

    private ResultActions performPushedAuthorizationRequestWithDPoP(final String clientId, final String attestation,
                                                                    final String dpopProof) throws Exception {
        return mockMvc.perform(post("/cas/oidc/" + OidcConstants.PUSHED_AUTHORIZE_URL)
            .param(OAuth20Constants.CLIENT_ID, clientId)
            .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org/")
            .param(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase(Locale.ENGLISH))
            .header(OidcClientAttestationAuthenticator.HEADER_CLIENT_ATTESTATION, attestation)
            .header(OAuth20Constants.DPOP, dpopProof)
            .with(withHttpRequestProcessor()));
    }

    private static String buildDPoPProof(final ECKey key) throws Exception {
        return new DefaultDPoPProofFactory(key, JWSAlgorithm.ES256)
            .createDPoPJWT("POST", new URI("https://sso.example.org/cas/oidc/" + OidcConstants.PUSHED_AUTHORIZE_URL))
            .serialize();
    }

    /**
     * Once challenges are turned on, the proof of possession must carry one handed out by CAS.
     */
    @Nested
    @TestPropertySource(properties = {
        "cas.authn.oidc.discovery.require-pushed-authorization-requests=true",
        "cas.authn.oidc.client-attestation.trust-anchors=classpath:client-attestation-root.pem",
        "cas.authn.oidc.client-attestation.challenge.enabled=true"
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
    }
}
