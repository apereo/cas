package org.apereo.cas.oidc.authn;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.oauth2.sdk.dpop.DefaultDPoPProofFactory;
import com.nimbusds.oauth2.sdk.token.DPoPAccessToken;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OAuth20ProofOfPossessionValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("OIDCWeb")
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
}
