package org.apereo.cas.oidc.token;

import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.BaseOidcTokenSigningAndEncryptionService;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

import java.util.Optional;

/**
 * This is {@link BaseOidcJsonWebKeyTokenSigningAndEncryptionService}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public abstract class BaseOidcJsonWebKeyTokenSigningAndEncryptionService extends BaseOidcTokenSigningAndEncryptionService {
    /**
     * The default keystore for OIDC tokens.
     */
    protected final LoadingCache<String, Optional<RsaJsonWebKey>> defaultJsonWebKeystoreCache;
    /**
     * The service keystore for OIDC tokens.
     */
    protected final LoadingCache<OidcRegisteredService, Optional<RsaJsonWebKey>> serviceJsonWebKeystoreCache;

    public BaseOidcJsonWebKeyTokenSigningAndEncryptionService(final LoadingCache<String, Optional<RsaJsonWebKey>> defaultJsonWebKeystoreCache,
                                                              final LoadingCache<OidcRegisteredService, Optional<RsaJsonWebKey>> serviceJsonWebKeystoreCache,
                                                              final String issuer) {
        super(issuer);
        this.defaultJsonWebKeystoreCache = defaultJsonWebKeystoreCache;
        this.serviceJsonWebKeystoreCache = serviceJsonWebKeystoreCache;
    }

    @Override
    @SneakyThrows
    public String encode(final OAuthRegisteredService service, final JwtClaims claims) {
        val svc = (OidcRegisteredService) service;
        LOGGER.debug("Attempting to produce token generated for service [{}]", svc);
        val jws = createJsonWebSignature(claims);
        LOGGER.debug("Generated claims to put into token are [{}]", claims.toJson());

        var innerJwt = svc.isSignIdToken() ? signToken(svc, jws) : jws.getCompactSerialization();
        if (shouldEncryptTokenFor(svc)) {
            innerJwt = encryptToken(svc, jws, innerJwt);
        }

        return innerJwt;
    }

    protected boolean shouldEncryptTokenFor(final OidcRegisteredService svc) {
        return svc.isEncryptIdToken() && StringUtils.isNotBlank(svc.getIdTokenEncryptionAlg()) && StringUtils.isNotBlank(svc.getIdTokenEncryptionEncoding());
    }

    @Override
    protected PublicJsonWebKey getJsonWebKeySigningKey() {
        val jwks = defaultJsonWebKeystoreCache.get(getIssuer());
        if (jwks.isEmpty()) {
            throw new IllegalArgumentException("No signing key could be found for issuer " + getIssuer());
        }
        return jwks.get();
    }

    protected String signToken(final OidcRegisteredService svc, final JsonWebSignature jws) throws Exception {
        LOGGER.debug("Fetching JSON web key to sign the token for : [{}]", svc.getClientId());
        val jsonWebKey = getJsonWebKeySigningKey();
        LOGGER.debug("Found JSON web key to sign the token: [{}]", jsonWebKey);
        if (jsonWebKey.getPrivateKey() == null) {
            throw new IllegalArgumentException("JSON web key used to sign the id token has no associated private key");
        }
        configureJsonWebSignatureForIdTokenSigning(svc, jws, jsonWebKey);
        return jws.getCompactSerialization();
    }

    protected String encryptToken(final OidcRegisteredService svc, final JsonWebSignature jws, final String innerJwt) {
        val jsonWebKey = getJsonWebKeyForEncryption(svc);
        return encryptIdToken(svc.getIdTokenEncryptionAlg(), svc.getIdTokenEncryptionEncoding(),
            jws.getKeyIdHeaderValue(), jsonWebKey.getPublicKey(), innerJwt);
    }

    protected JsonWebKey getJsonWebKeyForEncryption(final OidcRegisteredService svc) {
        LOGGER.debug("Service [{}] is set to encrypt tokens", svc);
        val jwks = this.serviceJsonWebKeystoreCache.get(svc);
        if (jwks.isEmpty()) {
            throw new IllegalArgumentException("Service " + svc.getServiceId()
                + " with client id " + svc.getClientId()
                + " is configured to encrypt tokens, yet no JSON web key is available");
        }
        val jsonWebKey = jwks.get();
        LOGGER.debug("Found JSON web key to encrypt the token: [{}]", jsonWebKey);
        if (jsonWebKey.getPublicKey() == null) {
            throw new IllegalArgumentException("JSON web key used to sign the token has no associated public key");
        }
        return jsonWebKey;
    }
}
