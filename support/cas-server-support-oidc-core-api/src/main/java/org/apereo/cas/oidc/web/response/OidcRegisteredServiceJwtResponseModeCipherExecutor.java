package org.apereo.cas.oidc.web.response;

import module java.base;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.token.OidcRegisteredServiceJwtCipherExecutor;
import org.apereo.cas.oidc.util.InternalJwtAccessTokenCipherExecutor;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.token.cipher.JwtTicketCipherExecutor;
import org.apereo.cas.token.cipher.RegisteredServiceJwtTicketCipherExecutor;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jspecify.annotations.NonNull;

/**
 * This is {@link OidcRegisteredServiceJwtResponseModeCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Getter
public class OidcRegisteredServiceJwtResponseModeCipherExecutor extends RegisteredServiceJwtTicketCipherExecutor
    implements OidcRegisteredServiceJwtCipherExecutor {
    /**
     * The default keystore for OIDC tokens.
     */
    protected final LoadingCache<@NonNull OidcJsonWebKeyCacheKey, JsonWebKeySet> defaultJsonWebKeystoreCache;

    /**
     * The service keystore for OIDC tokens.
     */
    protected final LoadingCache<@NonNull OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> registeredServiceJsonWebKeystoreCache;

    /**
     * OIDC issuer.
     */
    protected final OidcIssuerService oidcIssuerService;

    @Override
    public Optional<String> getSigningKey(final RegisteredService registeredService) {
        val jwks = OidcJsonWebKeyStoreUtils.fetchJsonWebKeySetForSigning(registeredService, this, false);
        return jwks.map(keys -> keys.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE));
    }

    @Override
    public Optional<String> getEncryptionKey(final RegisteredService registeredService) {
        val jwks = OidcJsonWebKeyStoreUtils.fetchJsonWebKeySetForEncryption(registeredService, this);
        return jwks.map(JsonWebKeySet::toJson);
    }

    @Override
    protected RegisteredServiceProperty.RegisteredServiceProperties getCipherStrategyTypeRegisteredServiceProperty(
        final RegisteredService registeredService) {
        return RegisteredServiceProperty.RegisteredServiceProperties.OIDC_RESPONSE_MODE_JWT_CIPHER_STRATEGY_TYPE;
    }

    @Override
    protected JwtTicketCipherExecutor createCipherExecutorInstance(
        final String encryptionKey, final String signingKey,
        final RegisteredService registeredService) {
        return InternalJwtAccessTokenCipherExecutor.get(signingKey, encryptionKey, registeredService, this);
    }


    @Override
    protected RegisteredServiceProperty.RegisteredServiceProperties getCipherOperationRegisteredServiceSigningEnabledProperty() {
        return RegisteredServiceProperty.RegisteredServiceProperties.OIDC_RESPONSE_MODE_JWT_CIPHER_SIGNING_ENABLED;
    }

    @Override
    protected RegisteredServiceProperty.RegisteredServiceProperties getCipherOperationRegisteredServiceEncryptionEnabledProperty() {
        return RegisteredServiceProperty.RegisteredServiceProperties.OIDC_RESPONSE_MODE_JWT_CIPHER_ENCRYPTION_ENABLED;
    }
}
