package org.apereo.cas.oidc.token;

import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.RsaJsonWebKey;
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

    public OidcIdTokenSigningAndEncryptionService(final LoadingCache<String, Optional<RsaJsonWebKey>> defaultJsonWebKeystoreCache,
                                                  final LoadingCache<OAuthRegisteredService, Optional<RsaJsonWebKey>> serviceJsonWebKeystoreCache,
                                                  final String issuer) {
        super(defaultJsonWebKeystoreCache, serviceJsonWebKeystoreCache, issuer);
    }

    @Override
    public String getJsonWebKeySigningAlgorithm(final OAuthRegisteredService service) {
        val svc = OidcRegisteredService.class.cast(service);
        if (StringUtils.isBlank(svc.getIdTokenSigningAlg())) {
            return super.getJsonWebKeySigningAlgorithm(service);
        }
        return svc.getIdTokenSigningAlg();
    }

    @Override
    protected String encryptToken(final OidcRegisteredService svc, final String innerJwt) {
        val jsonWebKey = getJsonWebKeyForEncryption(svc);
        return encryptToken(svc.getIdTokenEncryptionAlg(), svc.getIdTokenEncryptionEncoding(),
            jsonWebKey.getKeyId(), jsonWebKey.getPublicKey(), innerJwt);
    }

    /**
     * Should sign token for service?
     *
     * @param svc the svc
     * @return the boolean
     */
    @Override
    public boolean shouldSignToken(final OAuthRegisteredService svc) {
        if (svc instanceof OidcRegisteredService) {
            val service = (OidcRegisteredService) svc;
            if (AlgorithmIdentifiers.NONE.equalsIgnoreCase(service.getIdTokenSigningAlg())) {
                LOGGER.warn("ID token signing algorithm is set to none for [{}] and ID token will not be signed", svc.getServiceId());
                return false;
            }
            return service.isSignIdToken();
        }
        return false;
    }

    /**
     * Should encrypt token for service?
     *
     * @param svc the svc
     * @return the boolean
     */
    @Override
    public boolean shouldEncryptToken(final OAuthRegisteredService svc) {
        if (svc instanceof OidcRegisteredService) {
            val service = (OidcRegisteredService) svc;
            if (AlgorithmIdentifiers.NONE.equalsIgnoreCase(service.getIdTokenEncryptionAlg())) {
                LOGGER.warn("ID token encryption algorithm is set to none for [{}] and ID token will not be encrypted", service.getServiceId());
                return false;
            }
            return service.isEncryptIdToken()
                && StringUtils.isNotBlank(service.getIdTokenEncryptionAlg())
                && StringUtils.isNotBlank(service.getIdTokenEncryptionEncoding());
        }
        return false;
    }
}
