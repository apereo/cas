package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20RegisteredServiceJwtAccessTokenCipherExecutor;
import org.apereo.cas.token.cipher.JwtTicketCipherExecutor;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;

import java.io.Serializable;
import java.security.Key;
import java.util.Objects;
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
    protected final LoadingCache<String, Optional<PublicJsonWebKey>> defaultJsonWebKeystoreCache;

    /**
     * The service keystore for OIDC tokens.
     */
    protected final LoadingCache<OAuthRegisteredService, Optional<PublicJsonWebKey>> serviceJsonWebKeystoreCache;

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
        val oidcRegisteredService = OidcRegisteredService.class.cast(registeredService);
        val issuer = oidcIssuerService.determineIssuer(Optional.of(oidcRegisteredService));
        LOGGER.trace("Using issuer [{}] to determine JWKS from default keystore cache", issuer);
        val jwks = defaultJsonWebKeystoreCache.get(issuer);
        if (jwks.isEmpty()) {
            LOGGER.warn("No signing key could be found for issuer " + issuer);
            return Optional.empty();
        }
        return Optional.of(jwks.get().toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE));
    }

    @Override
    public Optional<String> getEncryptionKey(final RegisteredService registeredService) {
        if (!isEncryptionEnabledForRegisteredService(registeredService)) {
            return Optional.empty();
        }
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

    @Override
    protected JwtTicketCipherExecutor createCipherExecutorInstance(final String encryptionKey, final String signingKey,
                                                                   final RegisteredService registeredService,
                                                                   final CipherOperationsStrategyType type) {
        val cipher = new JwtTicketCipherExecutor(encryptionKey, signingKey,
            StringUtils.isNotBlank(encryptionKey), StringUtils.isNotBlank(signingKey), 0, 0) {
            @Override
            public String decode(final Serializable value, final Object[] parameters) {
                if (parameters.length > 0) {
                    val registeredService = (RegisteredService) parameters[0];
                    setEncryptionKey(getEncryptionKeyForDecryption(registeredService));
                }
                return super.decode(value, parameters);
            }

            @Override
            protected byte[] sign(final byte[] value) {
                if (EncodingUtils.isJsonWebKey(signingKey)) {
                    val oidcRegisteredService = OidcRegisteredService.class.cast(registeredService);
                    val issuer = oidcIssuerService.determineIssuer(Optional.of(oidcRegisteredService));
                    LOGGER.trace("Using issuer [{}] to determine signing key from default keystore cache", issuer);

                    val jwks = defaultJsonWebKeystoreCache.get(issuer);
                    if (Objects.requireNonNull(jwks).isPresent()) {
                        val jws = jwks.get();
                        val kid = jws.getKeyId();
                        if (StringUtils.isNotBlank(kid)) {
                            getCustomHeaders().put(JsonWebKey.KEY_ID_PARAMETER, kid);
                        }
                        val alg = StringUtils.defaultIfBlank(jws.getAlgorithm(), getSigningAlgorithmFor(jws.getPrivateKey()));
                        getCustomHeaders().put(JsonWebKey.ALGORITHM_PARAMETER, alg);
                        return signWith(value, alg);
                    }
                }
                return super.sign(value);
            }
        };
        if (EncodingUtils.isJsonWebKey(encryptionKey) || EncodingUtils.isJsonWebKey(signingKey)) {
            cipher.setEncryptionAlgorithm(KeyManagementAlgorithmIdentifiers.RSA_OAEP_256);
        }
        cipher.setCustomHeaders(CollectionUtils.wrap(CUSTOM_HEADER_REGISTERED_SERVICE_ID, registeredService.getId()));
        cipher.setStrategyType(type);
        return cipher;
    }

    private Key getEncryptionKeyForDecryption(final RegisteredService registeredService) {
        val svc = (OAuthRegisteredService) registeredService;
        if (svc instanceof OidcRegisteredService) {
            val jwks = this.serviceJsonWebKeystoreCache.get(svc);
            if (jwks.isEmpty()) {
                LOGGER.warn("Service " + svc.getServiceId()
                    + " with client id " + svc.getClientId()
                    + " is configured to encrypt tokens, yet no JSON web key is available");
                return null;
            }
            val jsonWebKey = jwks.get();
            LOGGER.debug("Found JSON web key to encrypt the token: [{}]", jsonWebKey);
            if (jsonWebKey.getPrivateKey() == null) {
                LOGGER.warn("JSON web key used to sign the token has no associated private key");
                return null;
            }
            return jsonWebKey.getPrivateKey();
        }
        return null;
    }
}
