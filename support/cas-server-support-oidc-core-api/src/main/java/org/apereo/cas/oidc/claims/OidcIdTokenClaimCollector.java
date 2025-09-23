package org.apereo.cas.oidc.claims;

import org.jose4j.jwt.JwtClaims;

import java.util.List;

/**
 * This is {@link OidcIdTokenClaimCollector}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
public interface OidcIdTokenClaimCollector {
    /**
     * Bean name.
     */
    String BEAN_NAME = "oidcIdTokenClaimCollector";

    /**
     * Conclude jwt claims.
     *
     * @param claims the claims
     */
    default void conclude(final JwtClaims claims) {
    }

    /**
     * Default system behavior when collecting ID token claims.
     *
     * @return the oidc ID token claim collector
     */
    static OidcIdTokenClaimCollector defaultCollector() {
        return (claims, name, values) -> {
            if (values.size() == 1) {
                claims.setClaim(name, values.getFirst());
            } else if (values.size() > 1) {
                claims.setClaim(name, values);
            }
        };
    }

    /**
     * Listable collector forces all claims to be collected
     * as list regardless of the number of values found for the claim.
     *
     * @return the oidc ID token claim collector
     */
    static OidcIdTokenClaimCollector listableCollector() {
        return JwtClaims::setClaim;
    }

    /**
     * Collect.
     *
     * @param claims the claims
     * @param name   the attribute
     * @param values the values
     */
    void collect(JwtClaims claims, String name, List<Object> values);
}
