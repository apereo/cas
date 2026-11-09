package org.apereo.cas.oidc.profile;

import module java.base;
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
        if (registeredService instanceof final OidcRegisteredService oidcService && isSigningRequested(oidcService)) {
            return oidcService.getUserInfoSigningAlg();
        }
        return super.getJsonWebKeySigningAlgorithm(registeredService, jsonWebKey);
    }

    /**
     * User info is returned as a JWT when the service asks for signing or encryption, and every JWT is signed:
     * a service that asks only for encryption gets the response signed with the default algorithm, then encrypted.
     * {@code none} as the signing algorithm means no signing was asked for.
     *
     * @param registeredService the registered service
     * @return true or false
     */
    @Override
    public boolean shouldSignToken(final OAuthRegisteredService registeredService) {
        return registeredService instanceof final OidcRegisteredService service
            && (isSigningRequested(service) || shouldEncryptToken(service));
    }

    /**
     * User info is encrypted when the service asks for an encryption algorithm other than {@code none}.
     *
     * @param registeredService the registered service
     * @return true or false
     */
    @Override
    public boolean shouldEncryptToken(final OAuthRegisteredService registeredService) {
        return registeredService instanceof final OidcRegisteredService service
            && StringUtils.isNotBlank(service.getUserInfoEncryptedResponseAlg())
            && !Strings.CI.equals(service.getUserInfoEncryptedResponseAlg(), AlgorithmIdentifiers.NONE);
    }

    private static boolean isSigningRequested(final OidcRegisteredService service) {
        return StringUtils.isNotBlank(service.getUserInfoSigningAlg())
            && !Strings.CI.equals(service.getUserInfoSigningAlg(), AlgorithmIdentifiers.NONE);
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
                .algorithm(svc.getUserInfoEncryptedResponseAlg())
                .encryptionMethod(svc.getUserInfoEncryptedResponseEncoding())
                .allowedAlgorithms(discoverySettings.getUserInfoEncryptionAlgValuesSupported())
                .allowedContentEncryptionAlgorithms(discoverySettings.getUserInfoEncryptionEncodingValuesSupported())
                .headers(Map.of(OAuth20Constants.CLIENT_ID, svc.getClientId()))
                .build()
                .encrypt(innerJwt);
        }
        return innerJwt;
    }
}
