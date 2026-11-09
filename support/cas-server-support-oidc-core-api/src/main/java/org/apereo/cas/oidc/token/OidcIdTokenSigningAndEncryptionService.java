package org.apereo.cas.oidc.token;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.jwt.JsonWebTokenEncryptor;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jws.AlgorithmIdentifiers;

/**
 * This is {@link OidcIdTokenSigningAndEncryptionService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class OidcIdTokenSigningAndEncryptionService extends BaseOidcJsonWebKeyTokenSigningAndEncryptionService {
    private final OidcServerDiscoverySettings discoverySettings;

    public OidcIdTokenSigningAndEncryptionService(
        final LoadingCache<OidcJsonWebKeyCacheKey, JsonWebKeySet> defaultJsonWebKeystoreCache,
        final LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> serviceJsonWebKeystoreCache,
        final OidcIssuerService issuerService,
        final OidcServerDiscoverySettings discoverySettings,
        final CasConfigurationProperties casProperties) {
        super(defaultJsonWebKeystoreCache, serviceJsonWebKeystoreCache, issuerService, casProperties);
        this.discoverySettings = discoverySettings;
    }

    @Override
    public String getJsonWebKeySigningAlgorithm(final OAuthRegisteredService registeredService,
                                                final JsonWebKey jsonWebKey) {
        val svc = (OidcRegisteredService) registeredService;
        if (StringUtils.isBlank(svc.getIdTokenSigningAlg())) {
            return super.getJsonWebKeySigningAlgorithm(registeredService, jsonWebKey);
        }
        return svc.getIdTokenSigningAlg();
    }

    /**
     * ID tokens are always signed. An unsigned ID token, whose algorithm is {@code none}, carries no proof of who issued it,
     * so a service that asks for {@code none} is refused.
     *
     * @param registeredService the registered service
     * @return true for OpenID Connect services
     */
    @Override
    public boolean shouldSignToken(final OAuthRegisteredService registeredService) {
        if (registeredService instanceof final OidcRegisteredService service) {
            if (AlgorithmIdentifiers.NONE.equalsIgnoreCase(service.getIdTokenSigningAlg())) {
                LOGGER.error("Service [{}] asks for unsigned ID tokens, which are not issued; choose an ID token signing algorithm "
                    + "other than 'none'", registeredService.getServiceId());
                throw new IllegalArgumentException("Unsigned ID tokens are not issued");
            }
            return true;
        }
        return false;
    }

    /**
     * ID tokens are encrypted when the service turns on encryption and defines the encryption algorithm and encoding;
     * {@code none} as the encryption algorithm means no encryption.
     *
     * @param registeredService the registered service
     * @return true or false
     */
    @Override
    public boolean shouldEncryptToken(final OAuthRegisteredService registeredService) {
        return registeredService instanceof final OidcRegisteredService service
            && service.isEncryptIdToken()
            && StringUtils.isNotBlank(service.getIdTokenEncryptionAlg())
            && !AlgorithmIdentifiers.NONE.equalsIgnoreCase(service.getIdTokenEncryptionAlg())
            && StringUtils.isNotBlank(service.getIdTokenEncryptionEncoding());
    }

    @Override
    public Set<String> getAllowedSigningAlgorithms(final OAuthRegisteredService registeredService) {
        return this.discoverySettings.getIdTokenSigningAlgValuesSupported();
    }

    @Override
    protected String encryptToken(final OAuthRegisteredService service, final String innerJwt) {
        if (service instanceof final OidcRegisteredService registeredService) {
            val jsonWebKey = getJsonWebKeyForEncryption(registeredService);
            if (jsonWebKey == null && registeredService.isIdTokenEncryptionOptional()) {
                return innerJwt;
            }

            return JsonWebTokenEncryptor.builder()
                .key(jsonWebKey.getPublicKey())
                .keyId(jsonWebKey.getKeyId())
                .algorithm(registeredService.getIdTokenEncryptionAlg())
                .encryptionMethod(registeredService.getIdTokenEncryptionEncoding())
                .allowedAlgorithms(discoverySettings.getIdTokenEncryptionAlgValuesSupported())
                .allowedContentEncryptionAlgorithms(discoverySettings.getIdTokenEncryptionEncodingValuesSupported())
                .headers(Map.of(OAuth20Constants.CLIENT_ID, registeredService.getClientId()))
                .build()
                .encrypt(innerJwt);
        }
        return innerJwt;
    }
}
