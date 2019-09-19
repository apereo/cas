package org.apereo.cas.oidc.token;

import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.BaseTokenSigningAndEncryptionService;
import org.apereo.cas.util.EncodingUtils;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTParser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwt.JwtClaims;

import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link BaseOidcJsonWebKeyTokenSigningAndEncryptionService}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public abstract class BaseOidcJsonWebKeyTokenSigningAndEncryptionService extends BaseTokenSigningAndEncryptionService {
    /**
     * The default keystore for OIDC tokens.
     */
    protected final LoadingCache<String, Optional<RsaJsonWebKey>> defaultJsonWebKeystoreCache;
    /**
     * The service keystore for OIDC tokens.
     */
    protected final LoadingCache<OAuthRegisteredService, Optional<RsaJsonWebKey>> serviceJsonWebKeystoreCache;

    public BaseOidcJsonWebKeyTokenSigningAndEncryptionService(final LoadingCache<String, Optional<RsaJsonWebKey>> defaultJsonWebKeystoreCache,
                                                              final LoadingCache<OAuthRegisteredService, Optional<RsaJsonWebKey>> serviceJsonWebKeystoreCache,
                                                              final String issuer) {
        super(issuer);
        this.defaultJsonWebKeystoreCache = defaultJsonWebKeystoreCache;
        this.serviceJsonWebKeystoreCache = serviceJsonWebKeystoreCache;
    }

    @Override
    @SneakyThrows
    public String encode(final OAuthRegisteredService service, final JwtClaims claims) {
        LOGGER.trace("Attempting to produce token generated for service [{}]", service);
        val svc = (OidcRegisteredService) service;
        LOGGER.debug("Generated claims to put into token are [{}]", claims.toJson());

        var innerJwt = signTokenIfNecessary(claims, svc);
        if (shouldEncryptToken(svc)) {
            innerJwt = encryptToken(svc, innerJwt);
        }

        return innerJwt;
    }

    private String signTokenIfNecessary(final JwtClaims claims, final OidcRegisteredService svc) {
        if (shouldSignToken(svc)) {
            LOGGER.debug("Fetching JSON web key to sign the token for : [{}]", svc.getClientId());
            val jsonWebKey = getJsonWebKeySigningKey();
            LOGGER.debug("Found JSON web key to sign the token: [{}]", jsonWebKey);
            if (jsonWebKey.getPrivateKey() == null) {
                throw new IllegalArgumentException("JSON web key used to sign the token has no associated private key");
            }
            return signToken(svc, claims, jsonWebKey);
        }
        return signToken(svc, claims, null);
    }

    /**
     * Encrypt token.
     *
     * @param svc      the svc
     * @param token the inner jwt
     * @return the string
     */
    protected abstract String encryptToken(OidcRegisteredService svc, String token);

    @Override
    protected PublicJsonWebKey getJsonWebKeySigningKey() {
        val jwks = defaultJsonWebKeystoreCache.get(getIssuer());
        if (Objects.requireNonNull(jwks).isEmpty()) {
            throw new IllegalArgumentException("No signing key could be found for issuer " + getIssuer());
        }
        return jwks.get();
    }

    @SneakyThrows
    @Override
    public JwtClaims decode(final String token, final Optional<OAuthRegisteredService> service) {
        if (service.isPresent()) {
            var jwt = JWTParser.parse(token);
            if (jwt instanceof EncryptedJWT) {
                val encryptionKey = getJsonWebKeyForEncryption(service.get());
                val decoded = EncodingUtils.decryptJwtValue(encryptionKey.getPrivateKey(), token);
                return super.decode(decoded, service);
            }
        }
        return super.decode(token, service);
    }

    /**
     * Gets json web key for encryption.
     *
     * @param svc the svc
     * @return the json web key for encryption
     */
    protected PublicJsonWebKey getJsonWebKeyForEncryption(final OAuthRegisteredService svc) {
        LOGGER.debug("Service [{}] is set to encrypt tokens", svc);
        val jwks = this.serviceJsonWebKeystoreCache.get(svc);
        if (Objects.requireNonNull(jwks).isEmpty()) {
            throw new IllegalArgumentException("Service " + svc.getServiceId()
                + " with client id " + svc.getClientId()
                + " is configured to encrypt tokens, yet no JSON web key is available");
        }
        val jsonWebKey = jwks.get();
        LOGGER.debug("Found JSON web key to encrypt the token: [{}]", jsonWebKey);
        if (jsonWebKey.getPublicKey() == null) {
            throw new IllegalArgumentException("JSON web key used to encrypt the token has no associated public key");
        }
        return jsonWebKey;
    }
}
