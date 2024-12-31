package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.util.InternalJwtAccessTokenCipherExecutor;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20RegisteredServiceJwtAccessTokenCipherExecutor;
import org.apereo.cas.token.cipher.JwtTicketCipherExecutor;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import java.util.Optional;

/**
 * This is {@link OidcRegisteredServiceJwtAccessTokenCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class OidcRegisteredServiceJwtAccessTokenCipherExecutor extends OAuth20RegisteredServiceJwtAccessTokenCipherExecutor
    implements OidcRegisteredServiceJwtCipherExecutor {
    /**
     * The default keystore for OIDC tokens.
     */
    protected final LoadingCache<OidcJsonWebKeyCacheKey, JsonWebKeySet> defaultJsonWebKeystoreCache;

    /**
     * The service keystore for OIDC tokens.
     */
    protected final LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> registeredServiceJsonWebKeystoreCache;

    /**
     * OIDC issuer.
     */
    protected final OidcIssuerService oidcIssuerService;

    @Override
    public Optional<String> getSigningKey(final RegisteredService registeredService) {
        if (!isSigningEnabledForRegisteredService(registeredService)) {
            return Optional.empty();
        }
        val result = super.getSigningKey(registeredService);
        if (result.isPresent()) {
            return result;
        }

        if (registeredService instanceof OidcRegisteredService oidcRegisteredService) {
            val jsonWebKeySet = OidcJsonWebKeyStoreUtils.fetchJsonWebKeySetForSigning(registeredService, this, true);
            if (jsonWebKeySet.isPresent()) {
                val signingKey = jsonWebKeySet.get().findJsonWebKey(oidcRegisteredService.getJwksKeyId(), null, null,
                    oidcRegisteredService.getJwtAccessTokenSigningAlg());
                return Optional.ofNullable(signingKey)
                    .map(key -> key.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE))
                    .or(() -> Optional.of(jsonWebKeySet.get().toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE)));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getEncryptionKey(final RegisteredService registeredService) {
        if (!isEncryptionEnabledForRegisteredService(registeredService)) {
            return Optional.empty();
        }
        val result = super.getEncryptionKey(registeredService);
        if (result.isPresent()) {
            return result;
        }

        if (registeredService instanceof OidcRegisteredService) {
            val jwks = OidcJsonWebKeyStoreUtils.fetchJsonWebKeySetForEncryption(registeredService, this);
            return jwks.map(JsonWebKeySet::toJson);
        }
        return result;
    }

    @Override
    protected JwtTicketCipherExecutor createCipherExecutorInstance(
        final String encryptionKey, final String signingKey,
        final RegisteredService registeredService) {
        val cipher = InternalJwtAccessTokenCipherExecutor.get(signingKey, encryptionKey, registeredService, this);
        return prepareCipherExecutor(cipher, registeredService);
    }
}
