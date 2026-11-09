package org.apereo.cas.oidc.profile;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.token.BaseOidcJsonWebKeyTokenSigningAndEncryptionService;
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
import org.springframework.http.MediaType;

/**
 * This is {@link OidcTokenIntrospectionSigningAndEncryptionService}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class OidcTokenIntrospectionSigningAndEncryptionService extends BaseOidcJsonWebKeyTokenSigningAndEncryptionService {
    private final OidcServerDiscoverySettings discoverySettings;

    public OidcTokenIntrospectionSigningAndEncryptionService(
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
        val algorithm = registeredService.getIntrospectionSignedResponseAlg();
        return StringUtils.isBlank(algorithm) || Strings.CI.equals(algorithm, AlgorithmIdentifiers.NONE)
            ? AlgorithmIdentifiers.RSA_USING_SHA512
            : algorithm;
    }

    @Override
    protected String getSigningMediaType() {
        return MediaType.parseMediaType(OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE).getSubtype();
    }

    /**
     * Introspection responses as JWTs are always signed, with the algorithm the service asks for, or else with the
     * default algorithm; unsigned JWTs are not issued, so {@code none} as the signing algorithm is not honored.
     *
     * @param registeredService the registered service
     * @return true
     */
    @Override
    public boolean shouldSignToken(final OAuthRegisteredService registeredService) {
        return true;
    }

    /**
     * Introspection responses are encrypted when the service asks for an encryption algorithm other than {@code none}.
     *
     * @param registeredService the registered service
     * @return true or false
     */
    @Override
    public boolean shouldEncryptToken(final OAuthRegisteredService registeredService) {
        return StringUtils.isNotBlank(registeredService.getIntrospectionEncryptedResponseAlg())
            && !Strings.CI.equals(registeredService.getIntrospectionEncryptedResponseAlg(), AlgorithmIdentifiers.NONE);
    }

    @Override
    public Set<String> getAllowedSigningAlgorithms(final OAuthRegisteredService registeredService) {
        return this.discoverySettings.getIntrospectionSignedResponseAlgValuesSupported();
    }

    @Override
    protected String encryptToken(final OAuthRegisteredService registeredService,
                                  final String innerJwt) {
        val jsonWebKey = getJsonWebKeyForEncryption(registeredService);
        return JsonWebTokenEncryptor.builder()
            .key(jsonWebKey.getPublicKey())
            .keyId(jsonWebKey.getKeyId())
            .algorithm(registeredService.getIntrospectionEncryptedResponseAlg())
            .encryptionMethod(registeredService.getIntrospectionEncryptedResponseEncoding())
            .allowedAlgorithms(discoverySettings.getIntrospectionEncryptedResponseAlgValuesSupported())
            .allowedContentEncryptionAlgorithms(discoverySettings.getIntrospectionEncryptedResponseEncodingValuesSupported())
            .headers(Map.of(OAuth20Constants.CLIENT_ID, registeredService.getClientId()))
            .build()
            .encrypt(innerJwt);
    }
}
