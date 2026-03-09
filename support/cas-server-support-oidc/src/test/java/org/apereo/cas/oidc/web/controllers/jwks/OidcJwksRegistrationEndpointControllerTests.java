package org.apereo.cas.oidc.web.controllers.jwks;

import module java.base;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.jwks.register.ClientJwksRegistrationRequest;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.OAuth20AuthenticationClientProvider;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.cipher.BasicIdentifiableKey;
import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
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
    @Autowired
    @Qualifier("oidcJwtClientProvider")
    private OAuth20AuthenticationClientProvider oidcJwtClientProvider;

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
    @ValueSource(strings = {"Ed25519", "EC", "RSA"})
    void verifyChallengeWithAccessToken(final String algorithm) throws Throwable {
        val accessToken = getAccessToken();
        when(accessToken.getScopes()).thenReturn(Set.of(OidcConstants.CLIENT_JWKS_REGISTRATION_SCOPE));
        ticketRegistry.addTicket(accessToken);

        val proof = buildProof(algorithm);
        val registrationRequest = new ClientJwksRegistrationRequest(proof.proof());
        val output = mockMvc.perform(post("/cas/oidc/" + OidcConstants.JWKS_URL + "/clients/register")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam(OAuth20Constants.TOKEN, accessToken.getId())
                .content(registrationRequest.toJson())
                .with(withHttpRequestProcessor()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jkt").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();
        val jkt = JsonPath.read(output, "$.jkt").toString();

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val registeredService = getOidcRegisteredService();
        registeredService.setClientId(UUID.randomUUID().toString());
        servicesManager.save(registeredService);

        val authenticator = getAuthenticator();
        val claims = getClaims(registeredService.getClientId(),
            oidcIssuerService.determineIssuer(Optional.of(registeredService)),
            registeredService.getClientId(), registeredService.getClientId());

        val signingAlg = switch (algorithm) {
            case "EC" -> AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256;
            case "RSA" -> AlgorithmIdentifiers.RSA_USING_SHA256;
            case "Ed25519" -> AlgorithmIdentifiers.EDDSA;
            default -> throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        };

        val jwt = switch (signingAlg) {
            case AlgorithmIdentifiers.EDDSA -> {
                val header = new JWSHeader.Builder(JWSAlgorithm.EdDSA)
                    .keyID(jkt)
                    .jwk(Objects.requireNonNull(proof.keyPair()).toPublicJWK())
                    .build();
                val jws = new JWSObject(header, new Payload(claims.toJson()));
                jws.sign(new Ed25519Signer(Objects.requireNonNull(proof.keyPair())));
                yield jws.serialize().getBytes(StandardCharsets.UTF_8);
            }
            default -> EncodingUtils.signJws(new BasicIdentifiableKey(jkt, proof.privateKey()),
                claims.toJson().getBytes(StandardCharsets.UTF_8), Map.of(), signingAlg);
        };

        val credentials = new UsernamePasswordCredentials(
            OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER,
            new String(jwt, StandardCharsets.UTF_8));

        val code = defaultOAuthCodeFactory.create(
            RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getAuthentication(),
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(),
            StringUtils.EMPTY, StringUtils.EMPTY,
            registeredService.getClientId(), new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        ticketRegistry.addTicket(code);
        request.addParameter(OAuth20Constants.CODE, code.getId());

        authenticator.validate(new CallContext(context, new JEESessionStore()), credentials);
        assertNotNull(credentials.getUserProfile());
    }

    private static Proof buildProof(final String algorithm) throws Throwable {
        if ("EC".equalsIgnoreCase(algorithm)) {
            val gen = KeyPairGenerator.getInstance(algorithm);
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
            return new Proof(jws.serialize(), keyPair.getPublic(), keyPair.getPrivate());
        }

        if ("RSA".equalsIgnoreCase(algorithm)) {
            val gen = KeyPairGenerator.getInstance(algorithm);
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
            return new Proof(jws.serialize(), keyPair.getPublic(), keyPair.getPrivate());
        }
        if ("Ed25519".equalsIgnoreCase(algorithm)) {
            val jwk = new OctetKeyPairGenerator(Curve.Ed25519).generate();
            val publicJwk = jwk.toPublicJWK();
            val header = new JWSHeader.Builder(JWSAlgorithm.EdDSA)
                .jwk(publicJwk)
                .type(new JOSEObjectType("register-key+jws"))
                .build();
            val jws = new JWSObject(header, new Payload("test"));
            jws.sign(new Ed25519Signer(jwk));
            return new Proof(jws.serialize(), jwk);
        }

        throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
    }

    private Authenticator getAuthenticator() {
        val client = (BaseClient) oidcJwtClientProvider.createClient();
        return client.getAuthenticator();
    }

    private record Proof(String proof, @Nullable PublicKey publicKey,
        @Nullable PrivateKey privateKey, @Nullable OctetKeyPair keyPair) {
        Proof(final String proof, final OctetKeyPair keyPair) {
            this(proof, null, null, keyPair);
        }

        Proof(final String proof, final PublicKey publicKey, final PrivateKey privateKey) {
            this(proof, publicKey, privateKey, null);
        }
    }
}
