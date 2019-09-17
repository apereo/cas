package org.apereo.cas.oidc.token;

import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20RegisteredServiceJwtAccessTokenCipherExecutor;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwk.RsaJsonWebKey;

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
public class OidcRegisteredServiceJwtAccessTokenCipherExecutor extends OAuth20RegisteredServiceJwtAccessTokenCipherExecutor {
    /**
     * The default keystore for OIDC tokens.
     */
    protected final LoadingCache<String, Optional<RsaJsonWebKey>> defaultJsonWebKeystoreCache;
    /**
     * The service keystore for OIDC tokens.
     */
    protected final LoadingCache<OAuthRegisteredService, Optional<RsaJsonWebKey>> serviceJsonWebKeystoreCache;

    /**
     * OIDC issuer.
     */
    protected final String issuer;

    @Override
    public Optional<String> getSigningKey(final RegisteredService registeredService) {
        val result = super.getSigningKey(registeredService);
        if (result.isPresent()) {
            return result;
        }
        val jwks = defaultJsonWebKeystoreCache.get(this.issuer);
        if (jwks.isEmpty()) {
            LOGGER.warn("No signing key could be found for issuer " + this.issuer);
            return Optional.empty();
        }
        return Optional.of(jwks.get().toJson());
    }

    @Override
    public Optional<String> getEncryptionKey(final RegisteredService registeredService) {
        val svc = (OAuthRegisteredService) registeredService;

        val result = super.getEncryptionKey(registeredService);
        if (result.isPresent()) {
            return result;
        }

        if (svc instanceof OidcRegisteredService) {
            val jwks = this.serviceJsonWebKeystoreCache.get(svc);
            if (jwks.isEmpty()) {
                LOGGER.warn("Service " + svc.getServiceId()
                    + " with client id " + svc.getClientId()
                    + " is configured to encrypt tokens, yet no JSON web key is available");
                return Optional.empty();
            }
            val jsonWebKey = jwks.get();
            LOGGER.debug("Found JSON web key to encrypt the token: [{}]", jsonWebKey);
            if (jsonWebKey.getPublicKey() == null) {
                LOGGER.warn("JSON web key used to sign the token has no associated public key");
                return Optional.empty();
            }
            return Optional.of(jwks.get().toJson());
        }
        return result;
    }
}
