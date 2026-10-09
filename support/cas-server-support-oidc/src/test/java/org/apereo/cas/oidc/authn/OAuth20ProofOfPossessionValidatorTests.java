package org.apereo.cas.oidc.authn;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20ClientAuthenticationMethods;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.oauth2.sdk.dpop.DefaultDPoPProofFactory;
import com.nimbusds.oauth2.sdk.token.DPoPAccessToken;
import com.nimbusds.openid.connect.sdk.Nonce;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OAuth20ProofOfPossessionValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("OIDCWeb")
@TestPropertySource(properties = "cas.authn.oidc.client-attestation.trust-anchors=classpath:client-attestation-root.pem")
class OAuth20ProofOfPossessionValidatorTests extends AbstractOidcTests {

    @Test
    void verifyDPoPProofWithConfidentialClient() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val code = addCode(principal, registeredService);

        val ecJwk = new ECKeyGenerator(Curve.P_256).keyID(UUID.randomUUID().toString()).generate();
        val proofFactory = new DefaultDPoPProofFactory(ecJwk, JWSAlgorithm.ES256);
        val tokenUri = new URI("https://sso.example.org/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL);
        val dpopProof = proofFactory.createDPoPJWT(HttpMethod.POST.name(), tokenUri);
        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(OAuth20Constants.DPOP, dpopProof.serialize())
                .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                .param(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType())
                .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org")
                .param(OAuth20Constants.CODE, code.getId()))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL)
            .with(withHttpRequestProcessor())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header(OAuth20Constants.DPOP, dpopProof.serialize())
            .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
            .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret())
            .param(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType())
            .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org")
            .param(OAuth20Constants.CODE, code.getId()));
    }

    @Test
    void verifyDPoPProofCannotBeReplayed() throws Throwable {
        val ecJWK = new ECKeyGenerator(Curve.P_256).keyID(UUID.randomUUID().toString()).generate();
        val proofFactory = new DefaultDPoPProofFactory(ecJWK, JWSAlgorithm.ES256);

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        val code = addCode(principal, registeredService);
        val tokenUri = new URI("https://sso.example.org/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL);
        val tokenDpopProof = proofFactory.createDPoPJWT(HttpMethod.POST.name(), tokenUri);

        val tokenResult = mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(OAuth20Constants.DPOP, tokenDpopProof.serialize())
                .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret())
                .param(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType())
                .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org")
                .param(OAuth20Constants.CODE, code.getId()))
            .andExpect(status().isOk())
            .andReturn();
        val accessToken = JsonPath.read(tokenResult.getResponse().getContentAsString(), "$.access_token").toString();

        val profileUri = new URI("https://sso.example.org/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.PROFILE_URL);
        val profileDpopProof = proofFactory.createDPoPJWT(HttpMethod.POST.name(), profileUri, new DPoPAccessToken(accessToken));

        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.PROFILE_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(OAuth20Constants.DPOP, profileDpopProof.serialize())
                .param(OAuth20Constants.TOKEN, accessToken))
            .andExpect(status().isOk());
        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.PROFILE_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(OAuth20Constants.DPOP, profileDpopProof.serialize())
                .param(OAuth20Constants.TOKEN, accessToken))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyClientAttestationAtTokenEndpoint() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setTokenEndpointAuthenticationMethod(OAuth20ClientAuthenticationMethods.ATTEST_JWT_CLIENT_AUTH.getType());
        servicesManager.save(registeredService);
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val instanceKey = new ECKeyGenerator(Curve.P_256).generate();

        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(OidcClientAttestationAuthenticator.HEADER_CLIENT_ATTESTATION,
                    buildClientAttestation(registeredService.getClientId(), instanceKey, false))
                .header(OidcClientAttestationAuthenticator.HEADER_CLIENT_ATTESTATION_POP,
                    buildClientAttestationProof(instanceKey, oidcServerDiscoverySettings.getIssuer()))
                .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                .param(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType())
                .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org")
                .param(OAuth20Constants.CODE, addCode(principal, registeredService).getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token").exists());

        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret())
                .param(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType())
                .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org")
                .param(OAuth20Constants.CODE, addCode(principal, registeredService).getId()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyClientAttestationInDPoPCombinedModeAtTokenEndpoint() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setTokenEndpointAuthenticationMethod(OAuth20ClientAuthenticationMethods.ATTEST_JWT_CLIENT_AUTH_DPOP.getType());
        servicesManager.save(registeredService);
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val instanceKey = new ECKeyGenerator(Curve.P_256).generate();
        val attestation = buildClientAttestation(registeredService.getClientId(), instanceKey, false);
        val tokenUri = new URI("https://sso.example.org/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL);
        val dpopProof = new DefaultDPoPProofFactory(instanceKey, JWSAlgorithm.ES256).createDPoPJWT(HttpMethod.POST.name(), tokenUri).serialize();

        performCombinedTokenRequest(registeredService, attestation, dpopProof, addCode(principal, registeredService).getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token_type").value(OAuth20Constants.TOKEN_TYPE_DPOP));
        performCombinedTokenRequest(registeredService, attestation, dpopProof, addCode(principal, registeredService).getId())
            .andExpect(status().isUnauthorized());
        val otherProof = new DefaultDPoPProofFactory(new ECKeyGenerator(Curve.P_256).generate(), JWSAlgorithm.ES256)
            .createDPoPJWT(HttpMethod.POST.name(), tokenUri).serialize();
        performCombinedTokenRequest(registeredService, attestation, otherProof, addCode(principal, registeredService).getId())
            .andExpect(status().isUnauthorized());

        registeredService.setTokenEndpointAuthenticationMethod(OAuth20ClientAuthenticationMethods.ATTEST_JWT_CLIENT_AUTH.getType());
        servicesManager.save(registeredService);
        val freshProof = new DefaultDPoPProofFactory(instanceKey, JWSAlgorithm.ES256).createDPoPJWT(HttpMethod.POST.name(), tokenUri).serialize();
        performCombinedTokenRequest(registeredService, attestation, freshProof, addCode(principal, registeredService).getId())
            .andExpect(status().isUnauthorized());
    }

    /**
     * Once DPoP nonces are turned on, every DPoP proof must carry one handed out by CAS.
     */
    @Nested
    @TestPropertySource(properties = {
        "cas.authn.oidc.client-attestation.trust-anchors=classpath:client-attestation-root.pem",
        "cas.authn.oidc.dpop.nonce.enabled=true"
    })
    class DPoPNonceTests extends AbstractOidcTests {
        @Test
        void verifyNonceAtTokenAndProfileEndpoints() throws Throwable {
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            servicesManager.save(registeredService);
            val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
            val code = addCode(principal, registeredService);
            val proofFactory = new DefaultDPoPProofFactory(new ECKeyGenerator(Curve.P_256).generate(), JWSAlgorithm.ES256);
            val tokenUri = new URI("https://sso.example.org/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL);

            val nonce = performTokenRequest(registeredService, code.getId(), proofFactory.createDPoPJWT(HttpMethod.POST.name(), tokenUri).serialize())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.USE_DPOP_NONCE))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andReturn().getResponse().getHeader(OAuth20Constants.DPOP_NONCE);
            assertNotNull(nonce);
            performTokenRequest(registeredService, code.getId(),
                proofFactory.createDPoPJWT(HttpMethod.POST.name(), tokenUri, new Nonce("unknown")).serialize())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.USE_DPOP_NONCE))
                .andExpect(header().exists(OAuth20Constants.DPOP_NONCE));
            val tokenResult = performTokenRequest(registeredService, code.getId(),
                proofFactory.createDPoPJWT(HttpMethod.POST.name(), tokenUri, new Nonce(nonce)).serialize())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token_type").value(OAuth20Constants.TOKEN_TYPE_DPOP))
                .andReturn();
            val accessToken = new DPoPAccessToken(JsonPath.read(tokenResult.getResponse().getContentAsString(), "$.access_token").toString());

            val profileUri = new URI("https://sso.example.org/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.PROFILE_URL);
            val profileNonce = performProfileRequest(accessToken, proofFactory.createDPoPJWT(HttpMethod.POST.name(), profileUri, accessToken).serialize())
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("error=\"use_dpop_nonce\"")))
                .andExpect(jsonPath("$.error").value(OAuth20Constants.USE_DPOP_NONCE))
                .andReturn().getResponse().getHeader(OAuth20Constants.DPOP_NONCE);
            assertNotNull(profileNonce);
            for (val provided : List.of(nonce, profileNonce)) {
                performProfileRequest(accessToken,
                    proofFactory.createDPoPJWT(HttpMethod.POST.name(), profileUri, accessToken, new Nonce(provided)).serialize())
                    .andExpect(status().isOk());
            }
        }

        @Test
        void verifyNonceInDPoPCombinedModeAtTokenEndpoint() throws Throwable {
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            registeredService.setTokenEndpointAuthenticationMethod(OAuth20ClientAuthenticationMethods.ATTEST_JWT_CLIENT_AUTH_DPOP.getType());
            servicesManager.save(registeredService);
            val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
            val instanceKey = new ECKeyGenerator(Curve.P_256).generate();
            val attestation = buildClientAttestation(registeredService.getClientId(), instanceKey, false);
            val tokenUri = new URI("https://sso.example.org/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL);
            val proofFactory = new DefaultDPoPProofFactory(instanceKey, JWSAlgorithm.ES256);

            val nonce = performCombinedTokenRequest(registeredService, attestation,
                proofFactory.createDPoPJWT(HttpMethod.POST.name(), tokenUri).serialize(), addCode(principal, registeredService).getId())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.USE_DPOP_NONCE))
                .andReturn().getResponse().getHeader(OAuth20Constants.DPOP_NONCE);
            assertNotNull(nonce);
            performCombinedTokenRequest(registeredService, attestation,
                proofFactory.createDPoPJWT(HttpMethod.POST.name(), tokenUri, new Nonce(nonce)).serialize(), addCode(principal, registeredService).getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token_type").value(OAuth20Constants.TOKEN_TYPE_DPOP));
        }

        private ResultActions performTokenRequest(final OidcRegisteredService registeredService, final String code,
                                                  final String dpopProof) throws Exception {
            return mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(OAuth20Constants.DPOP, dpopProof)
                .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret())
                .param(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType())
                .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org")
                .param(OAuth20Constants.CODE, code));
        }

        private ResultActions performProfileRequest(final DPoPAccessToken accessToken, final String dpopProof) throws Exception {
            return mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.PROFILE_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(OAuth20Constants.DPOP, dpopProof)
                .param(OAuth20Constants.TOKEN, accessToken.getValue()));
        }
    }

    private ResultActions performCombinedTokenRequest(final OidcRegisteredService registeredService, final String attestation,
                                                      final String dpopProof, final String code) throws Exception {
        return mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL)
            .with(withHttpRequestProcessor())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header(OidcClientAttestationAuthenticator.HEADER_CLIENT_ATTESTATION, attestation)
            .header(OAuth20Constants.DPOP, dpopProof)
            .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
            .param(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType())
            .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org")
            .param(OAuth20Constants.CODE, code));
    }
}
