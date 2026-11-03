package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.jwt.JsonWebTokenSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.val;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.zip.CompressionAlgorithmIdentifiers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultOidcRequestParameterResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("OIDC")
class DefaultOidcRequestParameterResolverTests extends AbstractOidcTests {
    @Test
    void verifyCredentialConfigurationScopeIsKept() {
        val properties = new CasConfigurationProperties();
        val configuration = new OidcVerifiableCredentialConfigurationProperties();
        configuration.setScope("UniversityDegree");
        properties.getAuthn().getOidc().getVc().getIssuer().getCredentialConfigurations().put("degree", configuration);
        val jwtBuilder = mock(JwtBuilder.class);
        when(jwtBuilder.getCasProperties()).thenReturn(properties);

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.SCOPE, "openid UniversityDegree UnknownScope");
        val scopes = new OidcRequestParameterResolver(jwtBuilder, mock(OidcRequestObjectDecrypter.class))
            .resolveRequestScopes(request);
        assertEquals(Set.of(OidcConstants.StandardScopes.OPENID.getScope(), "UniversityDegree"), scopes);
    }

    @Test
    void verifySignedJwtWithClientId() throws Throwable {
        val registeredService = getOidcRegisteredService("client");
        servicesManager.save(registeredService);

        val payload = JwtBuilder.JwtRequest.builder()
            .registeredService(Optional.of(registeredService))
            .serviceAudience(Set.of(UUID.randomUUID().toString()))
            .issuer("https://cas.example.org")
            .jwtId(UUID.randomUUID().toString())
            .subject("casuser")
            .issueDate(new Date())
            .attributes(Map.of(OAuth20Constants.SCOPE, List.of(OidcConstants.StandardScopes.OPENID.getScope()),
                OAuth20Constants.REDIRECT_URI, List.of("https://apereo.github.io"),
                OAuth20Constants.RESPONSE_TYPE, List.of("code"),
                OAuth20Constants.CLIENT_ID, List.of(registeredService.getClientId()),
                OAuth20Constants.PROMPT, List.of(OidcConstants.PROMPT_LOGIN)))
            .build();

        val jwtString = oidcAccessTokenJwtBuilder.build(payload);
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REQUEST, jwtString);
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());

        val scope = oauthRequestParameterResolver.resolveRequestParameter(request, OAuth20Constants.SCOPE, String.class);
        assertFalse(scope.isEmpty());
        assertTrue(scope.get().contains(OidcConstants.StandardScopes.OPENID.getScope()));
        assertEquals(OidcConstants.PROMPT_LOGIN, oauthRequestParameterResolver.resolveRequestParameter(request, OAuth20Constants.PROMPT).orElseThrow());
        assertTrue(oauthRequestParameterResolver.resolveRequestedPromptValues(request).isEmpty());
    }

    /**
     * OpenID Connect Core 1.0 section 6.3.1: a request object signed by the client and encrypted to an encryption key of
     * CAS is decrypted, and its nested JWT verified with the client's keys like any signed request object.
     */
    @Test
    void verifyEncryptedSignedRequestObject() throws Throwable {
        val service = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(service);
        val serviceKey = (PublicJsonWebKey) oidcServiceJsonWebKeystoreCache.get(
                new OidcJsonWebKeyCacheKey(service, OidcJsonWebKeyUsage.SIGNING)).orElseThrow().getJsonWebKeys()
            .stream()
            .filter(key -> !OidcJsonWebKeyUsage.ENCRYPTION.is(key))
            .findFirst()
            .orElseThrow();
        val signedJwt = JsonWebTokenSigner.builder()
            .key(serviceKey.getPrivateKey())
            .algorithm(serviceKey.getAlgorithm())
            .build()
            .sign(JwtClaims.parse(requestObjectClaims(service.getClientId()).toString()));

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REQUEST, encrypt(signedJwt, resolveEncryptionKey()));
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        val scopes = oauthRequestParameterResolver.resolveRequestParameter(request, OAuth20Constants.SCOPE, List.class).orElseThrow();
        assertEquals(List.of(OidcConstants.StandardScopes.OPENID.getScope(), OidcConstants.StandardScopes.PROFILE.getScope()), scopes);

        val tampered = new MockHttpServletRequest();
        tampered.addParameter(OAuth20Constants.REQUEST, encrypt(signedJwt.substring(0, signedJwt.lastIndexOf('.') + 1) + "AAAA",
            resolveEncryptionKey()));
        tampered.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        assertTrue(oauthRequestParameterResolver.isRequestObjectVerified(request));
        assertFalse(oauthRequestParameterResolver.isRequestObjectVerified(tampered));
        assertTrue(oauthRequestParameterResolver.resolveRequestParameter(tampered, OAuth20Constants.SCOPE, List.class).isEmpty());
    }

    @Test
    void verifyEncryptedUnsignedRequestObject() throws Throwable {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REQUEST, encrypt(requestObjectClaims("client").toString(), resolveEncryptionKey()));
        val scopes = oauthRequestParameterResolver.resolveRequestParameter(request, OAuth20Constants.SCOPE, List.class).orElseThrow();
        assertTrue(scopes.contains(OidcConstants.StandardScopes.OPENID.getScope()));
    }

    @Test
    void verifyRequestObjectEncryptedToUnknownKeyIsRefused() throws Throwable {
        val unknownKey = RsaJwkGenerator.generateJwk(2048);
        unknownKey.setKeyId(UUID.randomUUID().toString());
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REQUEST, encrypt(requestObjectClaims("client").toString(), unknownKey));
        assertThrows(IllegalArgumentException.class,
            () -> oauthRequestParameterResolver.resolveRequestParameter(request, OAuth20Constants.SCOPE, List.class));
        assertFalse(oauthRequestParameterResolver.isRequestObjectVerified(request));

        val compressed = new JsonWebEncryption();
        compressed.setPayload(requestObjectClaims("client").toString());
        compressed.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.RSA_OAEP_256);
        compressed.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_256_GCM);
        compressed.setCompressionAlgorithmHeaderParameter(CompressionAlgorithmIdentifiers.DEFLATE);
        val encryptionKey = resolveEncryptionKey();
        compressed.setKeyIdHeaderValue(encryptionKey.getKeyId());
        compressed.setKey(encryptionKey.getPublicKey());
        val compressedRequest = new MockHttpServletRequest();
        compressedRequest.addParameter(OAuth20Constants.REQUEST, compressed.getCompactSerialization());
        assertThrows(IllegalArgumentException.class,
            () -> oauthRequestParameterResolver.resolveRequestParameter(compressedRequest, OAuth20Constants.SCOPE, List.class));
        assertFalse(oauthRequestParameterResolver.isRequestObjectVerified(compressedRequest));
    }

    private PublicJsonWebKey resolveEncryptionKey() {
        val issuer = oidcIssuerService.determineIssuer(Optional.empty());
        return Objects.requireNonNull(oidcDefaultJsonWebKeystoreCache.get(new OidcJsonWebKeyCacheKey(issuer, OidcJsonWebKeyUsage.ENCRYPTION)))
            .getJsonWebKeys()
            .stream()
            .filter(RsaJsonWebKey.class::isInstance)
            .map(PublicJsonWebKey.class::cast)
            .findFirst()
            .orElseThrow();
    }

    private static JWTClaimsSet requestObjectClaims(final String clientId) {
        return new JWTClaimsSet.Builder()
            .subject("cas")
            .claim(OAuth20Constants.SCOPE, List.of(OidcConstants.StandardScopes.OPENID.getScope(), OidcConstants.StandardScopes.PROFILE.getScope()))
            .claim(OAuth20Constants.CLIENT_ID, clientId)
            .build();
    }

    private static String encrypt(final String payload, final PublicJsonWebKey key) throws Exception {
        val jwe = new JsonWebEncryption();
        jwe.setPayload(payload);
        jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.RSA_OAEP_256);
        jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_256_GCM);
        jwe.setContentTypeHeaderValue("JWT");
        jwe.setKeyIdHeaderValue(key.getKeyId());
        jwe.setKey(key.getPublicKey());
        return jwe.getCompactSerialization();
    }
}

