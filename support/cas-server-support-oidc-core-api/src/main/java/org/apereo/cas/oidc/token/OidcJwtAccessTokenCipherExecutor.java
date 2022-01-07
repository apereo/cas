package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link OidcJwtAccessTokenCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public class OidcJwtAccessTokenCipherExecutor extends BaseStringCipherExecutor {
    /**
     * The default keystore for OIDC tokens.
     */
    protected final LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> defaultJsonWebKeystoreCache;

    /**
     * OIDC issuer.
     */
    protected final OidcIssuerService oidcIssuerService;

    @Override
    public String getName() {
        return "OpenID Connect JWT Access Tokens";
    }

    @Override
    public String encode(final Serializable value, final Object[] parameters) {
        getJsonWebKeyFor(OidcJsonWebKeyUsage.SIGNING)
            .map(jwks -> jwks.getJsonWebKeys().get(0))
            .map(PublicJsonWebKey.class::cast)
            .ifPresent(key -> setSigningKey(key.getPrivateKey()));

        getJsonWebKeyFor(OidcJsonWebKeyUsage.ENCRYPTION)
            .map(jwks -> jwks.getJsonWebKeys().get(0))
            .ifPresent(key -> {
                setEncryptionKey(key.getKey());
                setContentEncryptionAlgorithmIdentifier(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
                setEncryptionAlgorithm(KeyManagementAlgorithmIdentifiers.RSA_OAEP_256);
            });
        return super.encode(value, parameters);
    }

    @Override
    public String decode(final Serializable value, final Object[] parameters) {
        getJsonWebKeyFor(OidcJsonWebKeyUsage.ENCRYPTION)
            .map(jwks -> jwks.getJsonWebKeys().get(0))
            .map(PublicJsonWebKey.class::cast)
            .ifPresent(key -> {
                setEncryptionKey(key.getPrivateKey());
                setContentEncryptionAlgorithmIdentifier(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
                setEncryptionAlgorithm(KeyManagementAlgorithmIdentifiers.RSA_OAEP_256);
            });
        getJsonWebKeyFor(OidcJsonWebKeyUsage.SIGNING)
            .map(jwks -> jwks.getJsonWebKeys().get(0))
            .map(PublicJsonWebKey.class::cast)
            .ifPresent(key -> setSigningKey(key.getPublicKey()));
        return super.decode(value, parameters);
    }

    private Optional<JsonWebKeySet> getJsonWebKeyFor(final OidcJsonWebKeyUsage usage) {
        val issuer = oidcIssuerService.determineIssuer(Optional.empty());
        LOGGER.trace("Determined issuer [{}] to fetch the JSON web key", issuer);
        return Objects.requireNonNull(defaultJsonWebKeystoreCache.get(new OidcJsonWebKeyCacheKey(issuer, usage)));
    }
}
