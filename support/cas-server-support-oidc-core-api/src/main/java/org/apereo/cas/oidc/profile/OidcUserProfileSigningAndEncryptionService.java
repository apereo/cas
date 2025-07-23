package org.apereo.cas.oidc.profile;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.token.BaseOidcJsonWebKeyTokenSigningAndEncryptionService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.jwt.JsonWebTokenEncryptor;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jws.AlgorithmIdentifiers;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link OidcUserProfileSigningAndEncryptionService}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class OidcUserProfileSigningAndEncryptionService extends BaseOidcJsonWebKeyTokenSigningAndEncryptionService {
    /**
     * Default encoding for user-info encrypted responses.
     */
    public static final String USER_INFO_RESPONSE_ENCRYPTION_ENCODING_DEFAULT = "A128CBC-HS256";

    private final OidcServerDiscoverySettings discoverySettings;

    public OidcUserProfileSigningAndEncryptionService(
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
        if (registeredService instanceof final OidcRegisteredService oidcService) {
            return oidcService.getUserInfoSigningAlg();
        }
        return super.getJsonWebKeySigningAlgorithm(registeredService, jsonWebKey);
    }

    @Override
    public boolean shouldSignToken(final OAuthRegisteredService registeredService) {
        if (registeredService instanceof final OidcRegisteredService service) {
            if (AlgorithmIdentifiers.NONE.equalsIgnoreCase(service.getUserInfoSigningAlg())
                && !discoverySettings.getUserInfoSigningAlgValuesSupported().contains(AlgorithmIdentifiers.NONE)) {
                LOGGER.error("Service [{}] has defined 'none' for user-info signing algorithm, "
                             + "yet CAS is configured to support the following signing algorithms: [{}]. "
                             + "This is quite likely due to misconfiguration of the CAS server or the service definition.",
                    registeredService.getServiceId(), discoverySettings.getUserInfoSigningAlgValuesSupported());
                throw new IllegalArgumentException("Unable to use 'none' as user-info signing algorithm");
            }
            return StringUtils.isNotBlank(service.getUserInfoSigningAlg())
                   && !Strings.CI.equals(service.getUserInfoSigningAlg(), AlgorithmIdentifiers.NONE);
        }
        return false;
    }

    @Override
    public boolean shouldEncryptToken(final OAuthRegisteredService registeredService) {
        if (registeredService instanceof final OidcRegisteredService service) {

            if (AlgorithmIdentifiers.NONE.equalsIgnoreCase(service.getUserInfoEncryptedResponseAlg())
                && !discoverySettings.getUserInfoEncryptionAlgValuesSupported().contains(AlgorithmIdentifiers.NONE)) {
                LOGGER.error("Service [{}] has defined 'none' for user-info encryption algorithm, "
                             + "yet CAS is configured to support the following encryption algorithms: [{}]. "
                             + "This is quite likely due to misconfiguration of the CAS server or the service definition",
                    registeredService.getServiceId(), discoverySettings.getUserInfoEncryptionAlgValuesSupported());
                throw new IllegalArgumentException("Unable to use 'none' as user-info encryption algorithm");
            }
            return StringUtils.isNotBlank(service.getUserInfoEncryptedResponseAlg())
                   && !Strings.CI.equals(service.getUserInfoEncryptedResponseAlg(), AlgorithmIdentifiers.NONE);
        }
        return false;
    }

    @Override
    public Set<String> getAllowedSigningAlgorithms(final OAuthRegisteredService registeredService) {
        return this.discoverySettings.getUserInfoSigningAlgValuesSupported();
    }

    @Override
    protected String encryptToken(final OAuthRegisteredService service,
                                  final String innerJwt) {
        if (service instanceof final OidcRegisteredService svc) {
            val jsonWebKey = getJsonWebKeyForEncryption(svc);
            return JsonWebTokenEncryptor.builder()
                .key(jsonWebKey.getPublicKey())
                .keyId(jsonWebKey.getKeyId())
                .algorithm(svc.getIdTokenEncryptionAlg())
                .encryptionMethod(svc.getIdTokenEncryptionEncoding())
                .allowedAlgorithms(discoverySettings.getUserInfoEncryptionAlgValuesSupported())
                .allowedContentEncryptionAlgorithms(discoverySettings.getUserInfoEncryptionEncodingValuesSupported())
                .headers(Map.of(OAuth20Constants.CLIENT_ID, svc.getClientId()))
                .build()
                .encrypt(innerJwt);
        }
        return innerJwt;
    }
}
