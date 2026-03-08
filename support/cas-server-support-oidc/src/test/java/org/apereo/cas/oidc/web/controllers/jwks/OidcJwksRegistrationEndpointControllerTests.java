package org.apereo.cas.oidc.web.controllers.jwks;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.jwks.register.ClientJwksRegistrationRequest;
import org.apereo.cas.support.oauth.OAuth20Constants;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcJwksRegistrationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("OIDCWeb")
@TestPropertySource(properties = "CasFeatureModule.OpenIDConnect.client-jwks-registration.enabled=true")
class OidcJwksRegistrationEndpointControllerTests extends AbstractOidcTests {
    @Test
    void verifyWithoutAccessToken() throws Exception {
        val content = new ClientJwksRegistrationRequest(UUID.randomUUID().toString()).toJson();
        mockMvc.perform(post("/cas/oidc/" + OidcConstants.JWKS_URL + "/clients/register")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(content)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyChallengeAccessTokenWithoutScope() throws Throwable {
        val accessToken = getAccessToken();
        ticketRegistry.addTicket(accessToken);
        val content = new ClientJwksRegistrationRequest(UUID.randomUUID().toString()).toJson();
        mockMvc.perform(post("/cas/oidc/" + OidcConstants.JWKS_URL + "/clients/register")
                .queryParam(OAuth20Constants.TOKEN, accessToken.getId())
                .content(content)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"EC", "RSA"})
    void verifyChallengeWithAccessToken(final String algorithm) throws Throwable {
        val accessToken = getAccessToken();
        when(accessToken.getScopes()).thenReturn(Set.of(OidcConstants.CLIENT_JWKS_REGISTRATION_SCOPE));
        ticketRegistry.addTicket(accessToken);

        val proof = buildProof(algorithm);
        val request = new ClientJwksRegistrationRequest(proof);
        mockMvc.perform(post("/cas/oidc/" + OidcConstants.JWKS_URL + "/clients/register")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam(OAuth20Constants.TOKEN, accessToken.getId())
                .content(request.toJson())
                .with(withHttpRequestProcessor()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jkt").exists());
    }

    private static String buildProof(final String algorithm) throws Throwable {
        val gen = KeyPairGenerator.getInstance(algorithm);
        if ("EC".equalsIgnoreCase(algorithm)) {
            gen.initialize(new ECGenParameterSpec("secp256r1"));
            val keyPair = gen.generateKeyPair();
            val publicJwk = new ECKey.Builder(Curve.P_256, (ECPublicKey) keyPair.getPublic()).build();
            val header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                .jwk(publicJwk)
                .type(new JOSEObjectType("register-key+jws"))
                .build();
            val jws = new JWSObject(header, new Payload("test"));
            val signer = new ECDSASigner((ECPrivateKey) keyPair.getPrivate());
            jws.sign(signer);
            return jws.serialize();
        }

        if ("RSA".equalsIgnoreCase(algorithm)) {
            gen.initialize(2048);
            val keyPair = gen.generateKeyPair();
            val publicJwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic()).build();
            val header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .jwk(publicJwk)
                .type(new JOSEObjectType("register-key+jws"))
                .build();
            val jws = new JWSObject(header, new Payload("test"));
            val signer = new RSASSASigner(keyPair.getPrivate());
            jws.sign(signer);
            return jws.serialize();
        }
        throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
    }

}
