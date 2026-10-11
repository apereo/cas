package org.apereo.cas.oidc.authn;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20ClientAuthenticationMethods;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.oauth2.sdk.dpop.DefaultDPoPProofFactory;
import com.nimbusds.oauth2.sdk.pkce.CodeChallenge;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.oauth2.sdk.token.DPoPAccessToken;
import com.nimbusds.openid.connect.sdk.Nonce;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
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
    private static final URI TOKEN_URI = URI.create("https://sso.example.org/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL);

    private static final URI PROFILE_URI = URI.create("https://sso.example.org/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.PROFILE_URL);

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

        performProfileRequest(new DPoPAccessToken(accessToken), profileDpopProof.serialize())
            .andExpect(status().isOk());
        performProfileRequest(new DPoPAccessToken(accessToken), profileDpopProof.serialize())
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

    @Test
    void verifyAuthorizationCodeBoundToDPoPKey() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        val key = new ECKeyGenerator(Curve.P_256).generate();
        val authentication = RegisteredServiceTestUtils.getAuthentication(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            Map.of(OAuth20Constants.DPOP_JKT, List.of(key.computeThumbprint().toString())));
        val code = defaultOAuthCodeFactory.create(webApplicationServiceFactory.createService(registeredService.getClientId()),
            authentication, new MockTicketGrantingTicket("casuser"), List.of(OidcConstants.StandardScopes.OPENID.getScope()),
            registeredService.getClientId(), OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        ticketRegistry.addTicket(code);
        val tokenUri = new URI("https://sso.example.org/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL);
        val otherProof = new DefaultDPoPProofFactory(new ECKeyGenerator(Curve.P_256).generate(), JWSAlgorithm.ES256)
            .createDPoPJWT(HttpMethod.POST.name(), tokenUri).serialize();

        for (val proof : Arrays.asList(null, otherProof)) {
            performTokenRequest(registeredService, code.getId(), proof)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_DPOP_PROOF));
        }
        performTokenRequest(registeredService, code.getId(),
            new DefaultDPoPProofFactory(key, JWSAlgorithm.ES256).createDPoPJWT(HttpMethod.POST.name(), tokenUri).serialize())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token_type").value(OAuth20Constants.TOKEN_TYPE_DPOP));
    }

    @Test
    void verifyClientWithDPoPBoundAccessTokens() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setDpopBoundAccessTokens(true);
        servicesManager.save(registeredService);
        val code = addCode(CoreAuthenticationTestUtils.getPrincipal("casuser"), registeredService);
        performTokenRequest(registeredService, code.getId(), null)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_DPOP_PROOF));

        val tokenUri = new URI("https://sso.example.org/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL);
        val proof = new DefaultDPoPProofFactory(new ECKeyGenerator(Curve.P_256).generate(), JWSAlgorithm.ES256)
            .createDPoPJWT(HttpMethod.POST.name(), tokenUri).serialize();
        performTokenRequest(registeredService, code.getId(), proof)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token_type").value(OAuth20Constants.TOKEN_TYPE_DPOP));
    }

    @Test
    void verifyDPoPBoundAccessTokenIsNotABearerToken() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        val code = addCode(CoreAuthenticationTestUtils.getPrincipal("casuser"), registeredService);
        val proofFactory = new DefaultDPoPProofFactory(new ECKeyGenerator(Curve.P_256).generate(), JWSAlgorithm.ES256);
        val tokenResult = performTokenRequest(registeredService, code.getId(),
                proofFactory.createDPoPJWT(HttpMethod.POST.name(), TOKEN_URI).serialize())
            .andExpect(status().isOk())
            .andReturn();
        val accessToken = new DPoPAccessToken(JsonPath.read(tokenResult.getResponse().getContentAsString(), "$.access_token").toString());
        val profileProof = proofFactory.createDPoPJWT(HttpMethod.POST.name(), PROFILE_URI, accessToken).serialize();

        val asBearer = profileRequest(profileProof)
            .header(HttpHeaders.AUTHORIZATION, OAuth20Constants.TOKEN_TYPE_BEARER + ' ' + accessToken.getValue());
        val asParameter = profileRequest(profileProof).param(OAuth20Constants.TOKEN, accessToken.getValue());
        for (val request : List.of(asBearer, asParameter)) {
            mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_TOKEN))
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer error=\"invalid_token\"")));
        }
        performProfileRequest(accessToken, profileProof).andExpect(status().isOk());
    }

    @Test
    void verifyMalformedOrRepeatedDPoPHeaderAtTokenEndpoint() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        val code = addCode(CoreAuthenticationTestUtils.getPrincipal("casuser"), registeredService);
        val proofFactory = new DefaultDPoPProofFactory(new ECKeyGenerator(Curve.P_256).generate(), JWSAlgorithm.ES256);

        performTokenRequest(registeredService, code.getId(), "not-a-jwt")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_DPOP_PROOF));
        mockMvc.perform(codeRequest(registeredService, code.getId(), proofFactory.createDPoPJWT(HttpMethod.POST.name(), TOKEN_URI).serialize())
                .header(OAuth20Constants.DPOP, proofFactory.createDPoPJWT(HttpMethod.POST.name(), TOKEN_URI).serialize()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_DPOP_PROOF));
        performTokenRequest(registeredService, code.getId(), proofFactory.createDPoPJWT(HttpMethod.POST.name(), TOKEN_URI).serialize())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token_type").value(OAuth20Constants.TOKEN_TYPE_DPOP));
    }

    @Test
    void verifyInvalidDPoPProofAtProfileEndpointIsChallenged() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        val code = addCode(CoreAuthenticationTestUtils.getPrincipal("casuser"), registeredService);
        val proofFactory = new DefaultDPoPProofFactory(new ECKeyGenerator(Curve.P_256).generate(), JWSAlgorithm.ES256);
        val tokenResult = performTokenRequest(registeredService, code.getId(),
                proofFactory.createDPoPJWT(HttpMethod.POST.name(), TOKEN_URI).serialize())
            .andExpect(status().isOk())
            .andReturn();
        val accessToken = new DPoPAccessToken(JsonPath.read(tokenResult.getResponse().getContentAsString(), "$.access_token").toString());
        val authorization = OAuth20Constants.TOKEN_TYPE_DPOP + ' ' + accessToken.getValue();
        val algs = "algs=\"%s\"".formatted(String.join(" ", casProperties.getAuthn().getOidc().getDiscovery().getDpopSigningAlgValuesSupported()));

        val withoutProof = post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.PROFILE_URL)
            .with(withHttpRequestProcessor())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header(HttpHeaders.AUTHORIZATION, authorization);
        val malformedProof = profileRequest("not-a-jwt").header(HttpHeaders.AUTHORIZATION, authorization);
        val repeatedProof = profileRequest(proofFactory.createDPoPJWT(HttpMethod.POST.name(), PROFILE_URI, accessToken).serialize())
            .header(OAuth20Constants.DPOP, proofFactory.createDPoPJWT(HttpMethod.POST.name(), PROFILE_URI, accessToken).serialize())
            .header(HttpHeaders.AUTHORIZATION, authorization);
        for (val request : List.of(withoutProof, malformedProof, repeatedProof)) {
            mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_DPOP_PROOF))
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("DPoP error=\"invalid_dpop_proof\"")))
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, endsWith(", " + algs)));
        }
        performProfileRequest(accessToken, proofFactory.createDPoPJWT(HttpMethod.POST.name(), PROFILE_URI, accessToken).serialize())
            .andExpect(status().isOk());
    }

    @Test
    void verifyRefreshTokenOfPublicClientBoundToDPoPKey() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setClientSecrets(new ArrayList<>());
        registeredService.setGenerateRefreshToken(true);
        registeredService.setRenewRefreshToken(true);
        servicesManager.save(registeredService);
        val verifier = new CodeVerifier();
        val code = defaultOAuthCodeFactory.create(webApplicationServiceFactory.createService(registeredService.getClientId()),
            RegisteredServiceTestUtils.getAuthentication("casuser"), new MockTicketGrantingTicket("casuser"),
            List.of(OidcConstants.StandardScopes.OPENID.getScope()), CodeChallenge.compute(CodeChallengeMethod.S256, verifier).getValue(),
            CodeChallengeMethod.S256.getValue(), registeredService.getClientId(), Map.of(), OAuth20ResponseTypes.CODE,
            OAuth20GrantTypes.AUTHORIZATION_CODE);
        ticketRegistry.addTicket(code);
        val proofFactory = new DefaultDPoPProofFactory(new ECKeyGenerator(Curve.P_256).generate(), JWSAlgorithm.ES256);
        val refreshToken = readRefreshToken(mockMvc.perform(codeRequest(registeredService, code.getId(),
            proofFactory.createDPoPJWT(HttpMethod.POST.name(), TOKEN_URI).serialize())
            .param(OAuth20Constants.CODE_VERIFIER, verifier.getValue())));

        val otherProof = new DefaultDPoPProofFactory(new ECKeyGenerator(Curve.P_256).generate(), JWSAlgorithm.ES256)
            .createDPoPJWT(HttpMethod.POST.name(), TOKEN_URI).serialize();
        for (val proof : Arrays.asList(null, otherProof)) {
            performRefreshTokenRequest(registeredService, refreshToken, proof)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_DPOP_PROOF));
        }
        val renewedRefreshToken = readRefreshToken(performRefreshTokenRequest(registeredService, refreshToken,
            proofFactory.createDPoPJWT(HttpMethod.POST.name(), TOKEN_URI).serialize())
            .andExpect(jsonPath("$.token_type").value(OAuth20Constants.TOKEN_TYPE_DPOP)));
        performRefreshTokenRequest(registeredService, renewedRefreshToken, null)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_DPOP_PROOF));
    }

    @Test
    void verifyRefreshTokenOfConfidentialClientNotBoundToDPoPKey() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setGenerateRefreshToken(true);
        servicesManager.save(registeredService);
        val code = addCode(CoreAuthenticationTestUtils.getPrincipal("casuser"), registeredService);
        val refreshToken = readRefreshToken(performTokenRequest(registeredService, code.getId(),
            new DefaultDPoPProofFactory(new ECKeyGenerator(Curve.P_256).generate(), JWSAlgorithm.ES256)
                .createDPoPJWT(HttpMethod.POST.name(), TOKEN_URI).serialize()));
        performRefreshTokenRequest(registeredService, refreshToken, null)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token_type").value(OAuth20Constants.TOKEN_TYPE_BEARER));
    }

    private ResultActions performTokenRequest(final OidcRegisteredService registeredService, final String code,
                                              @Nullable final String dpopProof)
            throws Exception {
        return mockMvc.perform(codeRequest(registeredService, code, dpopProof));
    }

    private MockHttpServletRequestBuilder codeRequest(final OidcRegisteredService registeredService, final String code,
                                                      @Nullable final String dpopProof) {
        return tokenRequest(registeredService, dpopProof)
            .param(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType())
            .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org")
            .param(OAuth20Constants.CODE, code);
    }

    private ResultActions performRefreshTokenRequest(final OidcRegisteredService registeredService, final String refreshToken,
                                                     @Nullable final String dpopProof)
            throws Exception {
        return mockMvc.perform(tokenRequest(registeredService, dpopProof)
            .param(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.getType())
            .param(OAuth20Constants.REFRESH_TOKEN, refreshToken));
    }

    private MockHttpServletRequestBuilder tokenRequest(final OidcRegisteredService registeredService, @Nullable final String dpopProof) {
        val request = post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL)
            .with(withHttpRequestProcessor())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        if (StringUtils.isNotBlank(registeredService.getClientSecret())) {
            request.param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret());
        }
        return dpopProof == null ? request : request.header(OAuth20Constants.DPOP, dpopProof);
    }

    private static String readRefreshToken(final ResultActions result) throws Exception {
        val response = result.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.refresh_token").toString();
    }

    private ResultActions performProfileRequest(final DPoPAccessToken accessToken, final String dpopProof) throws Exception {
        return mockMvc.perform(profileRequest(dpopProof)
            .header(HttpHeaders.AUTHORIZATION, OAuth20Constants.TOKEN_TYPE_DPOP + ' ' + accessToken.getValue()));
    }

    private MockHttpServletRequestBuilder profileRequest(final String dpopProof) {
        return post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.PROFILE_URL)
            .with(withHttpRequestProcessor())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header(OAuth20Constants.DPOP, dpopProof);
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
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("algs=\"%s\"".formatted(
                    String.join(" ", casProperties.getAuthn().getOidc().getDiscovery().getDpopSigningAlgValuesSupported())))))
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
    }

    private ResultActions performCombinedTokenRequest(final OidcRegisteredService registeredService, final String attestation,
                                                      final String dpopProof, final String code)
            throws Exception {
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
