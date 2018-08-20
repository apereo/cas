package org.apereo.cas.ticket;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;

/**
 * This is {@link IdTokenSigningAndEncryptionService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public interface IdTokenSigningAndEncryptionService {
    /**
     * Sign id token.
     *
     * @param svc    the service
     * @param claims the claims
     * @return the string
     */
    String encode(OAuthRegisteredService svc, JwtClaims claims);

    /**
     * Decode jwt claims.
     *
     * @param token the token
     * @return the jwt claims
     */
    JwtClaims validate(String token);

    /**
     * Gets json web key signing algorithm.
     *
     * @param svc the svc
     * @return the json web key signing algorithm
     */
    default String getJsonWebKeySigningAlgorithm(final OAuthRegisteredService svc) {
        return AlgorithmIdentifiers.RSA_USING_SHA256;
    }
}
