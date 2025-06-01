package org.apereo.cas.oidc.profile;

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
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.http.MediaType;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
        return StringUtils.defaultIfBlank(registeredService.getIntrospectionSignedResponseAlg(), AlgorithmIdentifiers.RSA_USING_SHA512);
    }

    @Override
    protected String getSigningMediaType() {
        return MediaType.parseMediaType(OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE).getSubtype();
    }

    @Override
    public boolean shouldSignToken(final OAuthRegisteredService registeredService) {
        if (AlgorithmIdentifiers.NONE.equalsIgnoreCase(registeredService.getIntrospectionSignedResponseAlg())
            && !discoverySettings.getIntrospectionSignedResponseAlgValuesSupported().contains(AlgorithmIdentifiers.NONE)) {
            LOGGER.error("Service [{}] has defined 'none' for introspection signing algorithm, "
                    + "yet CAS is configured to support the following signing algorithms: [{}]. "
                    + "This is quite likely due to misconfiguration of the CAS server or the service definition",
                registeredService.getServiceId(), discoverySettings.getIntrospectionSignedResponseAlgValuesSupported());
            throw new IllegalArgumentException("Unable to use 'none' as introspection signing algorithm");
        }
        return StringUtils.isNotBlank(registeredService.getIntrospectionSignedResponseAlg())
            && !StringUtils.equalsIgnoreCase(registeredService.getIntrospectionSignedResponseAlg(), AlgorithmIdentifiers.NONE);
    }

    @Override
    public boolean shouldEncryptToken(final OAuthRegisteredService registeredService) {
        if (AlgorithmIdentifiers.NONE.equalsIgnoreCase(registeredService.getIntrospectionEncryptedResponseAlg())
            && !discoverySettings.getIntrospectionEncryptedResponseAlgValuesSupported().contains(AlgorithmIdentifiers.NONE)) {
            LOGGER.error("Service [{}] has defined 'none' for introspection encryption algorithm, "
                    + "yet CAS is configured to support the following encryption algorithms: [{}]. "
                    + "This is quite likely due to misconfiguration of the CAS server or the service definition",
                registeredService.getServiceId(), discoverySettings.getIntrospectionEncryptedResponseAlgValuesSupported());
            throw new IllegalArgumentException("Unable to use 'none' as introspection encryption algorithm");
        }
        return StringUtils.isNotBlank(registeredService.getIntrospectionEncryptedResponseAlg())
            && !StringUtils.equalsIgnoreCase(registeredService.getIntrospectionEncryptedResponseAlg(), AlgorithmIdentifiers.NONE);
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
