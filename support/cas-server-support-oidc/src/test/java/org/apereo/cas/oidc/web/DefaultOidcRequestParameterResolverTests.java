package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreModifiedEvent;
import org.apereo.cas.oidc.jwks.rotation.OidcJsonWebKeystoreRotatedEvent;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.validator.OAuth20ClientSecretValidator;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.jwt.JsonWebTokenSigner;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.zip.CompressionAlgorithmIdentifiers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
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
        val scopes = new OidcRequestParameterResolver(jwtBuilder, mock(OidcRequestObjectDecrypter.class), mock(OAuth20ClientSecretValidator.class))
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

    /**
     * OpenID Connect Core 1.0, sections 6.1 and 10.1: a request object may be signed with HMAC using the client secret,
     * whose octets must be at least as long as the hash output of the algorithm.
     */
    @Test
    void verifyRequestObjectSignedWithClientSecret() throws Throwable {
        val service = getOidcRegisteredService(UUID.randomUUID().toString());
        service.setClientSecrets(new ArrayList<>());
        service.setClientSecret(RandomUtils.randomAlphanumeric(64));
        servicesManager.save(service);
        for (val algorithm : List.of(JWSAlgorithm.HS256, JWSAlgorithm.HS384, JWSAlgorithm.HS512)) {
            val request = requestWith(service.getClientId(), signWithSecret(algorithm, service.getClientSecret(), service.getClientId()));
            assertTrue(oauthRequestParameterResolver.isRequestObjectVerified(request), algorithm.getName());
            assertEquals(List.of(OidcConstants.StandardScopes.OPENID.getScope(), OidcConstants.StandardScopes.PROFILE.getScope()),
                oauthRequestParameterResolver.resolveRequestParameter(request, OAuth20Constants.SCOPE, List.class).orElseThrow());
        }
        assertFalse(oauthRequestParameterResolver.isRequestObjectVerified(
            requestWith(service.getClientId(), signWithSecret(JWSAlgorithm.HS256, RandomUtils.randomAlphanumeric(64), service.getClientId()))));
        val unknownClientId = UUID.randomUUID().toString();
        val unknownClientRequest = requestWith(unknownClientId, signWithSecret(JWSAlgorithm.HS256, service.getClientSecret(), unknownClientId));
        assertFalse(oauthRequestParameterResolver.isRequestObjectVerified(unknownClientRequest));
        assertTrue(oauthRequestParameterResolver.resolveRequestParameter(unknownClientRequest, OAuth20Constants.SCOPE).isEmpty());

        val shortSecretService = getOidcRegisteredService(UUID.randomUUID().toString());
        shortSecretService.setClientSecrets(new ArrayList<>());
        shortSecretService.setClientSecret(RandomUtils.randomAlphanumeric(40));
        servicesManager.save(shortSecretService);
        assertTrue(oauthRequestParameterResolver.isRequestObjectVerified(requestWith(shortSecretService.getClientId(),
            signWithSecret(JWSAlgorithm.HS256, shortSecretService.getClientSecret(), shortSecretService.getClientId()))));
        assertFalse(oauthRequestParameterResolver.isRequestObjectVerified(requestWith(shortSecretService.getClientId(),
            signWithShortSecret(shortSecretService.getClientSecret(), shortSecretService.getClientId()))));
    }

    /**
     * Client secrets of a request object signed with HMAC are checked for expiry and decoded by the client secret
     * validator, as they are when the client authenticates with them.
     */
    @Test
    void verifyRequestObjectSecretsFollowClientSecretValidator() throws Throwable {
        val service = getOidcRegisteredService(UUID.randomUUID().toString());
        service.setClientSecrets(new ArrayList<>());
        service.setClientSecret(RandomUtils.randomAlphanumeric(64));
        servicesManager.save(service);
        val requestObject = signWithSecret(JWSAlgorithm.HS256, service.getClientSecret(), service.getClientId());

        val validator = mock(OAuth20ClientSecretValidator.class);
        when(validator.getCipherExecutor()).thenReturn(CipherExecutor.noOpOfSerializableToString());
        when(validator.isClientSecretExpired(any(), any())).thenReturn(Boolean.FALSE);
        val resolver = new OidcRequestParameterResolver(oidcAccessTokenJwtBuilder, mock(OidcRequestObjectDecrypter.class), validator);
        assertTrue(resolver.isRequestObjectVerified(requestWith(service.getClientId(), requestObject)));

        when(validator.isClientSecretExpired(any(), any())).thenReturn(Boolean.TRUE);
        assertFalse(resolver.isRequestObjectVerified(requestWith(service.getClientId(), requestObject)));
        verify(validator, atLeastOnce()).isClientSecretExpired(any(), any());
    }

    /**
     * The request object must name the client of the request (OpenID Connect Core 1.0, section 6.1; RFC 9101,
     * section 5): it is verified with the keys of the {@code client_id} parameter, so a request object for another
     * client, even one signed with the keys of the client of the parameter, is refused, and its parameters are not used.
     */
    @Test
    void verifyRequestObjectForAnotherClientIsRefused() throws Throwable {
        val service = getOidcRegisteredService(UUID.randomUUID().toString());
        service.setClientSecrets(new ArrayList<>());
        service.setClientSecret(RandomUtils.randomAlphanumeric(64));
        servicesManager.save(service);
        val otherClientId = UUID.randomUUID().toString();
        val request = requestWith(service.getClientId(), signWithSecret(JWSAlgorithm.HS256, service.getClientSecret(), otherClientId));
        assertFalse(oauthRequestParameterResolver.isRequestObjectVerified(request));
        assertEquals(service.getClientId(), oauthRequestParameterResolver.resolveRequestParameter(request, OAuth20Constants.CLIENT_ID).orElseThrow());
        assertTrue(oauthRequestParameterResolver.resolveRequestParameter(request, OAuth20Constants.SCOPE, List.class).isEmpty());

        val sameClient = requestWith(service.getClientId(), signWithSecret(JWSAlgorithm.HS256, service.getClientSecret(), service.getClientId()));
        assertTrue(oauthRequestParameterResolver.isRequestObjectVerified(sameClient));
    }

    /**
     * The encryption keys are read from the keystore once per JWKS cache period rather than for every encrypted request
     * object, and {@code RSA1_5} is refused even when a deployment lists it (RFC 8725, section 3.2).
     */
    @Test
    void verifyDecryptionKeysAreCachedAndRsa15IsRefused() throws Throwable {
        val encryptionKey = RsaJwkGenerator.generateJwk(2048);
        encryptionKey.setKeyId(UUID.randomUUID().toString());
        encryptionKey.setUse("enc");
        val keystore = new JsonWebKeySet(encryptionKey).toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
        val generator = mock(OidcJsonWebKeystoreGeneratorService.class);
        when(generator.generate()).thenReturn(new ByteArrayResource(keystore.getBytes(StandardCharsets.UTF_8)));
        val properties = new CasConfigurationProperties();
        val discovery = properties.getAuthn().getOidc().getDiscovery();
        discovery.setRequestObjectEncryptionAlgValuesSupported(Stream.concat(discovery.getRequestObjectEncryptionAlgValuesSupported().stream(),
            Stream.of(KeyManagementAlgorithmIdentifiers.RSA1_5)).toList());
        val decrypter = new OidcRequestObjectDecrypter(() -> generator, properties);

        val payload = requestObjectClaims("client").toString();
        for (var attempt = 0; attempt < 2; attempt++) {
            val decrypted = JWTParser.parse(decrypter.decrypt(encrypt(payload, encryptionKey))).getJWTClaimsSet();
            assertEquals("client", decrypted.getStringClaim(OAuth20Constants.CLIENT_ID));
        }
        verify(generator, times(1)).generate();
        decrypter.handleOidcJsonWebKeystoreRotatedEvent(new OidcJsonWebKeystoreRotatedEvent(this,
            ClientInfo.from(new MockHttpServletRequest())));
        assertEquals(1, decrypter.getDecryptionKeys().size());
        verify(generator, times(2)).generate();

        val rsa15 = new JsonWebEncryption();
        rsa15.setPayload(payload);
        rsa15.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.RSA1_5);
        rsa15.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
        rsa15.setKeyIdHeaderValue(encryptionKey.getKeyId());
        rsa15.setKey(encryptionKey.getPublicKey());
        rsa15.setAlgorithmConstraints(AlgorithmConstraints.NO_CONSTRAINTS);
        val rsa15RequestObject = rsa15.getCompactSerialization();
        assertThrows(IllegalArgumentException.class, () -> decrypter.decrypt(rsa15RequestObject));
    }

    /**
     * A keystore without encryption keys is not read again for every encrypted request object, but is once it changes.
     */
    @Test
    void verifyMissingDecryptionKeysAreNotReadEachTime() throws Throwable {
        val signingKey = RsaJwkGenerator.generateJwk(2048);
        signingKey.setKeyId(UUID.randomUUID().toString());
        signingKey.setUse("sig");
        val keystore = new JsonWebKeySet(signingKey).toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
        val generator = mock(OidcJsonWebKeystoreGeneratorService.class);
        when(generator.generate()).thenReturn(new ByteArrayResource(keystore.getBytes(StandardCharsets.UTF_8)));
        val decrypter = new OidcRequestObjectDecrypter(() -> generator, new CasConfigurationProperties());
        assertTrue(decrypter.getDecryptionKeys().isEmpty());
        assertTrue(decrypter.getDecryptionKeys().isEmpty());
        verify(generator, times(1)).generate();
        decrypter.handleOidcJsonWebKeystoreModifiedEvent(new OidcJsonWebKeystoreModifiedEvent(this, new File("keystore.jwks"),
            ClientInfo.from(new MockHttpServletRequest())));
        assertTrue(decrypter.getDecryptionKeys().isEmpty());
        verify(generator, times(2)).generate();
    }

    /**
     * A request object encrypted without being signed proves nothing about who sent it, since anyone can encrypt to the
     * keys CAS publishes: it is refused as any unsigned request object is, and none of its parameters are used.
     */
    @Test
    void verifyEncryptedUnsignedRequestObjectIsRefused() throws Throwable {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REQUEST, encrypt(requestObjectClaims("client").toString(), resolveEncryptionKey()));
        assertTrue(oauthRequestParameterResolver.resolveRequestParameter(request, OAuth20Constants.SCOPE, List.class).isEmpty());
        assertFalse(oauthRequestParameterResolver.isRequestObjectVerified(request));
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

    private static String signWithSecret(final JWSAlgorithm algorithm, final String secret, final String clientId) throws Exception {
        val jwt = new SignedJWT(new JWSHeader(algorithm), requestObjectClaims(clientId));
        jwt.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));
        return jwt.serialize();
    }

    private static String signWithShortSecret(final String secret, final String clientId) throws Exception {
        val input = Base64URL.encode(new JWSHeader(JWSAlgorithm.HS512).toString()) + "." + Base64URL.encode(requestObjectClaims(clientId).toString());
        val mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        return input + '.' + Base64URL.encode(mac.doFinal(input.getBytes(StandardCharsets.UTF_8)));
    }

    private static MockHttpServletRequest requestWith(final String clientId, final String requestObject) {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REQUEST, requestObject);
        request.addParameter(OAuth20Constants.CLIENT_ID, clientId);
        return request;
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

