package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
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
 * This is {@link OidcIdTokenSigningAndEncryptionService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class OidcIdTokenSigningAndEncryptionService extends BaseOidcJsonWebKeyTokenSigningAndEncryptionService {
    private final OidcServerDiscoverySettings discoverySettings;

    public OidcIdTokenSigningAndEncryptionService(final LoadingCache<String, Optional<PublicJsonWebKey>> defaultJsonWebKeystoreCache,
                                                  final LoadingCache<OAuthRegisteredService, Optional<PublicJsonWebKey>> serviceJsonWebKeystoreCache,
                                                  final OidcIssuerService issuerService,
                                                  final OidcServerDiscoverySettings discoverySettings) {
        super(defaultJsonWebKeystoreCache, serviceJsonWebKeystoreCache, issuerService);
        this.discoverySettings = discoverySettings;
    }

    @Override
    public String getJsonWebKeySigningAlgorithm(final OAuthRegisteredService service) {
        val svc = OidcRegisteredService.class.cast(service);
        if (StringUtils.isBlank(svc.getIdTokenSigningAlg())) {
            return super.getJsonWebKeySigningAlgorithm(service);
        }
        return svc.getIdTokenSigningAlg();
    }

    /**
     * Should sign token for service?
     *
     * @param svc the svc
     * @return true/false
     */
    @Override
    public boolean shouldSignToken(final OAuthRegisteredService svc) {
        if (svc instanceof OidcRegisteredService) {
            val service = (OidcRegisteredService) svc;
            if (!service.isSignIdToken()) {
                LOGGER.trace("Service [{}] does not require ID token to be signed", svc.getServiceId());
                return false;
            }
            if (service.isSignIdToken() && AlgorithmIdentifiers.NONE.equalsIgnoreCase(service.getIdTokenSigningAlg())) {
                if (!discoverySettings.getIdTokenSigningAlgValuesSupported().contains(AlgorithmIdentifiers.NONE)) {
                    LOGGER.error("Service [{}] has defined 'none' for ID token signing algorithm, "
                            + "yet CAS is configured to support the following signing algorithms: [{}]. "
                            + "This is quite likely due to misconfiguration of the CAS server or the service definition",
                        svc.getServiceId(), discoverySettings.getIdTokenSigningAlgValuesSupported());
                    throw new IllegalArgumentException("Unable to use 'none' as ID token signing algorithm");
                }
                LOGGER.error("Service [{}] has defined 'none' for ID token signing algorithm", svc.getServiceId());
                return false;
            }
            return service.isSignIdToken();
        }
        return false;
    }

    @Override
    public boolean shouldEncryptToken(final OAuthRegisteredService svc) {
        if (svc instanceof OidcRegisteredService) {
            val service = (OidcRegisteredService) svc;
            
            if (service.isEncryptIdToken() && AlgorithmIdentifiers.NONE.equalsIgnoreCase(service.getIdTokenEncryptionAlg())) {
                if (!discoverySettings.getIdTokenSigningAlgValuesSupported().contains(AlgorithmIdentifiers.NONE)) {
                    LOGGER.error("Service [{}] has defined 'none' for ID token encryption algorithm, "
                            + "yet CAS is configured to support the following encryption algorithms: [{}]. "
                            + "This is quite likely due to misconfiguration of the CAS server or the service definition",
                        svc.getServiceId(), discoverySettings.getIdTokenEncryptionAlgValuesSupported());
                    throw new IllegalArgumentException("Unable to use 'none' as ID token encryption algorithm");
                }
                LOGGER.error("Service [{}] has defined 'none' for ID token encryption algorithm", svc.getServiceId());
                return false;
            }

            return service.isEncryptIdToken()
                && StringUtils.isNotBlank(service.getIdTokenEncryptionAlg())
                && StringUtils.isNotBlank(service.getIdTokenEncryptionEncoding());
        }
        return false;
    }

    @Override
    protected String encryptToken(final OAuthRegisteredService service, final String innerJwt) {
        if (service instanceof OidcRegisteredService) {
            val svc = OidcRegisteredService.class.cast(service);
            val jsonWebKey = getJsonWebKeyForEncryption(svc);
            return encryptToken(svc.getIdTokenEncryptionAlg(), svc.getIdTokenEncryptionEncoding(),
                jsonWebKey.getKeyId(), jsonWebKey.getPublicKey(), innerJwt);
        }
        return innerJwt;
    }
}
