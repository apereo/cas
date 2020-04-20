package org.apereo.cas.oidc.profile;

import org.apereo.cas.oidc.token.BaseOidcJsonWebKeyTokenSigningAndEncryptionService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import com.github.benmanes.caffeine.cache.LoadingCache;
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
public class OidcUserProfileSigningAndEncryptionService extends BaseOidcJsonWebKeyTokenSigningAndEncryptionService {
    /**
     * Default encoding for user-info encrypted responses.
     */
    public static final String USER_INFO_RESPONSE_ENCRYPTION_ENCODING_DEFAULT = "A128CBC-HS256";

    public OidcUserProfileSigningAndEncryptionService(final LoadingCache<String, Optional<PublicJsonWebKey>> defaultJsonWebKeystoreCache,
                                                      final LoadingCache<OAuthRegisteredService, Optional<PublicJsonWebKey>> serviceJsonWebKeystoreCache,
                                                      final String issuer) {
        super(defaultJsonWebKeystoreCache, serviceJsonWebKeystoreCache, issuer);
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
            return StringUtils.isNotBlank(service.getUserInfoSigningAlg())
                && !AlgorithmIdentifiers.NONE.equalsIgnoreCase(service.getIdTokenSigningAlg());
        }
        return false;
    }

    @Override
    public boolean shouldEncryptToken(final OAuthRegisteredService svc) {
        if (svc instanceof OidcRegisteredService) {
            val service = (OidcRegisteredService) svc;
            return StringUtils.isNotBlank(service.getUserInfoEncryptedResponseAlg())
                && !StringUtils.equalsIgnoreCase(service.getUserInfoEncryptedResponseAlg(), AlgorithmIdentifiers.NONE);
        }
        return false;
    }
}
