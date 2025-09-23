package org.apereo.cas.ticket;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;

import java.util.Optional;

/**
 * This is {@link OAuth20TokenSigningAndEncryptionService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public interface OAuth20TokenSigningAndEncryptionService {
    /**
     * Sign ID token.
     *
     * @param registeredService the service
     * @param claims            the claims
     * @return the string
     * @throws Throwable the throwable
     */
    String encode(OAuthRegisteredService registeredService, JwtClaims claims) throws Throwable;

    /**
     * Decode jwt claims.
     *
     * @param token   the token
     * @param registeredService the service
     * @return the jwt claims
     */
    JwtClaims decode(String token, Optional<OAuthRegisteredService> registeredService);

    /**
     * Gets json web key signing algorithm.
     *
     * @param registeredService        the svc
     * @param signingKey the signing key
     * @return the json web key signing algorithm
     */
    default String getJsonWebKeySigningAlgorithm(final OAuthRegisteredService registeredService,
                                                 final JsonWebKey signingKey) {
        var defaultAlgorithm = AlgorithmIdentifiers.RSA_USING_SHA256;
        if (signingKey instanceof EllipticCurveJsonWebKey) {
            defaultAlgorithm = AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256;
        }
        return StringUtils.defaultIfBlank(signingKey.getAlgorithm(), defaultAlgorithm);
    }

    /**
     * Gets json web key used as the signing key.
     *
     * @param registeredService the service result
     * @return the json web key signing key
     * @throws Throwable the throwable
     */
    PublicJsonWebKey getJsonWebKeySigningKey(Optional<OAuthRegisteredService> registeredService) throws Throwable;

    /**
     * Should sign token for service?
     *
     * @param registeredService the svc
     * @return true/false
     */
    default boolean shouldSignToken(final OAuthRegisteredService registeredService) {
        return false;
    }

    /**
     * Should encrypt token for service?
     *
     * @param registeredService the svc
     * @return true/false
     */
    default boolean shouldEncryptToken(final OAuthRegisteredService registeredService) {
        return false;
    }

    /**
     * Resolve issuer string.
     *
     * @param registeredService the service
     * @return the string
     */
    String resolveIssuer(Optional<OAuthRegisteredService> registeredService);
}
