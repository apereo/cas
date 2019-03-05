package org.apereo.cas.oidc.token;

import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.JsonWebSignature;

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
                                                  final LoadingCache<OidcRegisteredService, Optional<RsaJsonWebKey>> serviceJsonWebKeystoreCache,
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
    protected String encryptToken(final OidcRegisteredService svc, final JsonWebSignature jws, final String innerJwt) {
        val jsonWebKey = getJsonWebKeyForEncryption(svc);
        return encryptIdToken(svc.getIdTokenEncryptionAlg(), svc.getIdTokenEncryptionEncoding(),
            jws.getKeyIdHeaderValue(), jsonWebKey.getPublicKey(), innerJwt);
    }

    /**
     * Should sign token for service?
     *
     * @param svc the svc
     * @return the boolean
     */
    @Override
    protected boolean shouldSignTokenFor(final OidcRegisteredService svc) {
        return svc.isSignIdToken();
    }

    /**
     * Should encrypt token for service?
     *
     * @param svc the svc
     * @return the boolean
     */
    @Override
    protected boolean shouldEncryptTokenFor(final OidcRegisteredService svc) {
        return svc.isEncryptIdToken() && StringUtils.isNotBlank(svc.getIdTokenEncryptionAlg()) && StringUtils.isNotBlank(svc.getIdTokenEncryptionEncoding());
    }
}
