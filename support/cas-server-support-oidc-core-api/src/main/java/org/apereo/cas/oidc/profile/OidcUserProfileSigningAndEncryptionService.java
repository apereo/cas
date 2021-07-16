package org.apereo.cas.oidc.profile;

import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.token.BaseOidcJsonWebKeyTokenSigningAndEncryptionService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;

import java.util.Optional;

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

    public OidcUserProfileSigningAndEncryptionService(final LoadingCache<String, Optional<PublicJsonWebKey>> defaultJsonWebKeystoreCache,
                                                      final LoadingCache<OAuthRegisteredService, Optional<PublicJsonWebKey>> serviceJsonWebKeystoreCache,
                                                      final OidcIssuerService issuerService,
                                                      final OidcServerDiscoverySettings discoverySettings) {
        super(defaultJsonWebKeystoreCache, serviceJsonWebKeystoreCache, issuerService);
        this.discoverySettings = discoverySettings;
    }

    @Override
    public String getJsonWebKeySigningAlgorithm(final OAuthRegisteredService svc) {
        if (svc instanceof OidcRegisteredService) {
            return OidcRegisteredService.class.cast(svc).getUserInfoSigningAlg();
        }
        return super.getJsonWebKeySigningAlgorithm(svc);
    }

    @Override
    public boolean shouldSignToken(final OAuthRegisteredService svc) {
        if (svc instanceof OidcRegisteredService) {
            val service = (OidcRegisteredService) svc;

            if (AlgorithmIdentifiers.NONE.equalsIgnoreCase(service.getUserInfoSigningAlg())
                && !discoverySettings.getUserInfoSigningAlgValuesSupported().contains(AlgorithmIdentifiers.NONE)) {
                LOGGER.error("Service [{}] has defined 'none' for user-info signing algorithm, "
                        + "yet CAS is configured to support the following signing algorithms: [{}]. "
                        + "This is quite likely due to misconfiguration of the CAS server or the service definition",
                    svc.getServiceId(), discoverySettings.getUserInfoSigningAlgValuesSupported());
                throw new IllegalArgumentException("Unable to use 'none' as user-info signing algorithm");
            }
            return StringUtils.isNotBlank(service.getUserInfoSigningAlg())
                && !StringUtils.equalsIgnoreCase(service.getUserInfoSigningAlg(), AlgorithmIdentifiers.NONE);
        }
        return false;
    }

    @Override
    public boolean shouldEncryptToken(final OAuthRegisteredService svc) {
        if (svc instanceof OidcRegisteredService) {
            val service = (OidcRegisteredService) svc;

            if (AlgorithmIdentifiers.NONE.equalsIgnoreCase(service.getUserInfoEncryptedResponseAlg())
                && !discoverySettings.getUserInfoEncryptionAlgValuesSupported().contains(AlgorithmIdentifiers.NONE)) {
                LOGGER.error("Service [{}] has defined 'none' for user-info encryption algorithm, "
                        + "yet CAS is configured to support the following encryption algorithms: [{}]. "
                        + "This is quite likely due to misconfiguration of the CAS server or the service definition",
                    svc.getServiceId(), discoverySettings.getUserInfoEncryptionAlgValuesSupported());
                throw new IllegalArgumentException("Unable to use 'none' as user-info encryption algorithm");
            }
            return StringUtils.isNotBlank(service.getUserInfoEncryptedResponseAlg())
                && !StringUtils.equalsIgnoreCase(service.getUserInfoEncryptedResponseAlg(), AlgorithmIdentifiers.NONE);
        }
        return false;
    }

    @Override
    protected String encryptToken(final OAuthRegisteredService service,
                                  final String innerJwt) {
        if (service instanceof OidcRegisteredService) {
            val svc = OidcRegisteredService.class.cast(service);
            val jsonWebKey = getJsonWebKeyForEncryption(svc);
            return encryptToken(svc.getUserInfoEncryptedResponseAlg(),
                svc.getUserInfoEncryptedResponseEncoding(),
                jsonWebKey.getKeyId(),
                jsonWebKey.getPublicKey(),
                innerJwt);
        }
        return innerJwt;
    }
}
