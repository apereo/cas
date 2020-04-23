package org.apereo.cas.oidc.token;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.BaseTokenSigningAndEncryptionService;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.EncodingUtils;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTParser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwk.PublicJsonWebKey;
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
    protected final LoadingCache<String, Optional<PublicJsonWebKey>> defaultJsonWebKeystoreCache;

    /**
     * The service keystore for OIDC tokens.
     */
    protected final LoadingCache<OAuthRegisteredService, Optional<PublicJsonWebKey>> serviceJsonWebKeystoreCache;

    public BaseOidcJsonWebKeyTokenSigningAndEncryptionService(final LoadingCache<String, Optional<PublicJsonWebKey>> defaultJsonWebKeystoreCache,
                                                              final LoadingCache<OAuthRegisteredService, Optional<PublicJsonWebKey>> serviceJsonWebKeystoreCache,
                                                              final String issuer) {
        super(issuer);
        this.defaultJsonWebKeystoreCache = defaultJsonWebKeystoreCache;
        this.serviceJsonWebKeystoreCache = serviceJsonWebKeystoreCache;
    }

    @Override
    @SneakyThrows
    public String encode(final OAuthRegisteredService service, final JwtClaims claims) {
        LOGGER.trace("Attempting to produce token generated for service [{}] with claims [{}]", service, claims.toJson());
        var innerJwt = signTokenIfNecessary(claims, service);
        if (shouldEncryptToken(service)) {
            innerJwt = encryptToken(service, innerJwt);
        }

        return innerJwt;
    }

    private String signTokenIfNecessary(final JwtClaims claims, final OAuthRegisteredService svc) {
        if (shouldSignToken(svc)) {
            LOGGER.debug("Fetching JSON web key to sign the token for : [{}]", svc.getClientId());
            val jsonWebKey = getJsonWebKeySigningKey();
            LOGGER.debug("Found JSON web key to sign the token: [{}]", jsonWebKey);
            if (jsonWebKey.getPrivateKey() == null) {
                throw new IllegalArgumentException("JSON web key used to sign the token has no associated private key");
            }
            return signToken(svc, claims, jsonWebKey);
        }
        val claimSet = JwtBuilder.parse(claims.toJson());
        return JwtBuilder.buildPlain(claimSet, Optional.of(svc));
    }

    /**
     * Encrypt token.
     *
     * @param svc   the svc
     * @param token the inner jwt
     * @return the string
     */
    protected abstract String encryptToken(OAuthRegisteredService svc, String token);

    @Override
    protected PublicJsonWebKey getJsonWebKeySigningKey() {
        val jwks = defaultJsonWebKeystoreCache.get(getIssuer());
        if (Objects.requireNonNull(jwks).isEmpty()) {
            throw new IllegalArgumentException("No signing key could be found for issuer " + getIssuer());
        }
        return jwks.get();
    }

    @Override
    public JwtClaims decode(final String token, final Optional<OAuthRegisteredService> service) {
        try {
            if (service.isPresent()) {
                val jwt = JWTParser.parse(token);
                if (jwt instanceof EncryptedJWT) {
                    val encryptionKey = getJsonWebKeyForEncryption(service.get());
                    val decoded = EncodingUtils.decryptJwtValue(encryptionKey.getPrivateKey(), token);
                    return super.decode(decoded, service);
                }
            }
            return super.decode(token, service);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
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
