package org.apereo.cas.oidc.token;

import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.BaseIdTokenSigningAndEncryptionService;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

import java.util.Optional;

/**
 * This is {@link OidcIdTokenSigningAndEncryptionService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class OidcIdTokenSigningAndEncryptionService extends BaseIdTokenSigningAndEncryptionService {
    private final LoadingCache<String, Optional<RsaJsonWebKey>> defaultJsonWebKeystoreCache;
    private final LoadingCache<OidcRegisteredService, Optional<RsaJsonWebKey>> serviceJsonWebKeystoreCache;

    public OidcIdTokenSigningAndEncryptionService(final LoadingCache<String, Optional<RsaJsonWebKey>> defaultJsonWebKeystoreCache,
                                                  final LoadingCache<OidcRegisteredService, Optional<RsaJsonWebKey>> serviceJsonWebKeystoreCache,
                                                  final String issuer) {
        super(issuer);
        this.defaultJsonWebKeystoreCache = defaultJsonWebKeystoreCache;
        this.serviceJsonWebKeystoreCache = serviceJsonWebKeystoreCache;
    }

    @Override
    @SneakyThrows
    public String encode(final OAuthRegisteredService service, final JwtClaims claims) {
        val svc = OidcRegisteredService.class.cast(service);
        LOGGER.debug("Attempting to produce id token generated for service [{}]", svc);
        val jws = createJsonWebSignature(claims);
        LOGGER.debug("Generated claims to put into id token are [{}]", claims.toJson());

        var innerJwt = svc.isSignIdToken() ? signIdToken(svc, jws) : jws.getCompactSerialization();
        if (svc.isEncryptIdToken() && StringUtils.isNotBlank(svc.getIdTokenEncryptionAlg()) && StringUtils.isNotBlank(svc.getIdTokenEncryptionEncoding())) {
            innerJwt = encryptIdToken(svc, jws, innerJwt);
        }

        return innerJwt;
    }

    private String encryptIdToken(final OidcRegisteredService svc, final JsonWebSignature jws, final String innerJwt) throws Exception {
        LOGGER.debug("Service [{}] is set to encrypt id tokens", svc);
        val jwks = this.serviceJsonWebKeystoreCache.get(svc);
        if (!jwks.isPresent()) {
            throw new IllegalArgumentException("Service " + svc.getServiceId()
                + " with client id " + svc.getClientId()
                + " is configured to encrypt id tokens, yet no JSON web key is available");
        }
        val jsonWebKey = jwks.get();
        LOGGER.debug("Found JSON web key to encrypt the id token: [{}]", jsonWebKey);
        if (jsonWebKey.getPublicKey() == null) {
            throw new IllegalArgumentException("JSON web key used to sign the id token has no associated public key");
        }
        return encryptIdToken(svc.getIdTokenEncryptionAlg(), svc.getIdTokenEncryptionEncoding(),
            jws.getKeyIdHeaderValue(), jsonWebKey.getPublicKey(), innerJwt);
    }

    private String signIdToken(final OidcRegisteredService svc, final JsonWebSignature jws) throws Exception {
        LOGGER.debug("Fetching JSON web key to sign the id token for : [{}]", svc.getClientId());
        val jsonWebKey = getSigningKey();
        LOGGER.debug("Found JSON web key to sign the id token: [{}]", jsonWebKey);
        if (jsonWebKey.getPrivateKey() == null) {
            throw new IllegalArgumentException("JSON web key used to sign the id token has no associated private key");
        }
        configureJsonWebSignatureForIdTokenSigning(svc, jws, jsonWebKey);
        return jws.getCompactSerialization();
    }


    @Override
    protected PublicJsonWebKey getSigningKey() {
        val jwks = defaultJsonWebKeystoreCache.get(getIssuer());
        if (!jwks.isPresent()) {
            throw new IllegalArgumentException("No signing key could be found for issuer " + getIssuer());
        }
        return jwks.get();
    }

    @Override
    public String getJsonWebKeySigningAlgorithm(final OAuthRegisteredService service) {
        val svc = OidcRegisteredService.class.cast(service);
        if (StringUtils.isBlank(svc.getIdTokenSigningAlg())) {
            return super.getJsonWebKeySigningAlgorithm(service);
        }
        return svc.getIdTokenSigningAlg();
    }
}
