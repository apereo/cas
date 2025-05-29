package org.apereo.cas.oidc.token;

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

import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    @Override
    public boolean shouldSignToken(final OAuthRegisteredService registeredService) {
        if (registeredService instanceof final OidcRegisteredService service) {
            if (!service.isSignIdToken()) {
                LOGGER.trace("Service [{}] does not require ID token to be signed", registeredService.getServiceId());
                return false;
            }
            if (AlgorithmIdentifiers.NONE.equalsIgnoreCase(service.getIdTokenSigningAlg())) {
                if (!discoverySettings.getIdTokenSigningAlgValuesSupported().contains(AlgorithmIdentifiers.NONE)) {
                    LOGGER.error("Service [{}] has defined 'none' for ID token signing algorithm, "
                            + "yet CAS is configured to support the following signing algorithms: [{}]. "
                            + "This is quite likely due to misconfiguration of the CAS server or the service definition",
                        registeredService.getServiceId(), discoverySettings.getIdTokenSigningAlgValuesSupported());
                    throw new IllegalArgumentException("Unable to use 'none' as ID token signing algorithm");
                }
                LOGGER.error("Service [{}] has defined 'none' for ID token signing algorithm", registeredService.getServiceId());
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldEncryptToken(final OAuthRegisteredService registeredService) {
        if (registeredService instanceof final OidcRegisteredService service) {
            if (service.isEncryptIdToken() && AlgorithmIdentifiers.NONE.equalsIgnoreCase(service.getIdTokenEncryptionAlg())) {
                if (!discoverySettings.getIdTokenSigningAlgValuesSupported().contains(AlgorithmIdentifiers.NONE)) {
                    LOGGER.error("Service [{}] has defined 'none' for ID token encryption algorithm, "
                            + "yet CAS is configured to support the following encryption algorithms: [{}]. "
                            + "This is quite likely due to misconfiguration of the CAS server or the service definition",
                        registeredService.getServiceId(), discoverySettings.getIdTokenEncryptionAlgValuesSupported());
                    throw new IllegalArgumentException("Unable to use 'none' as ID token encryption algorithm");
                }
                LOGGER.error("Service [{}] has defined 'none' for ID token encryption algorithm", registeredService.getServiceId());
                return false;
            }

            return service.isEncryptIdToken()
                && StringUtils.isNotBlank(service.getIdTokenEncryptionAlg())
                && StringUtils.isNotBlank(service.getIdTokenEncryptionEncoding());
        }
        return false;
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
