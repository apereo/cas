package org.apereo.cas.ticket;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

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
     * Sign id token.
     *
     * @param service    the service
     * @param claims the claims
     * @return the string
     */
    String encode(OAuthRegisteredService service, JwtClaims claims);

    /**
     * Decode jwt claims.
     *
     * @param token   the token
     * @param service the service
     * @return the jwt claims
     */
    JwtClaims decode(String token, Optional<OAuthRegisteredService> service);

    /**
     * Gets json web key signing algorithm.
     *
     * @param svc the svc
     * @return the json web key signing algorithm
     */
    default String getJsonWebKeySigningAlgorithm(final OAuthRegisteredService svc) {
        return AlgorithmIdentifiers.RSA_USING_SHA256;
    }

    /**
     * Gets issuer.
     *
     * @return the issuer
     */
    String getIssuer();

    /**
     * Should sign token for service?
     *
     * @param svc the svc
     * @return true/false
     */
    default boolean shouldSignToken(final OAuthRegisteredService svc) {
        return false;
    }

    /**
     * Should encrypt token for service?
     *
     * @param svc the svc
     * @return true/false
     */
    default boolean shouldEncryptToken(final OAuthRegisteredService svc) {
        return false;
    }
}
