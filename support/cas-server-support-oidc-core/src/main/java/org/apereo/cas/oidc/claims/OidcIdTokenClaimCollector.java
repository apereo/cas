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
     * Default system behavior when collecting id token claims.
     *
     * @return the oidc id token claim collector
     */
    static OidcIdTokenClaimCollector defaultCollector() {
        return (claims, name, values) -> {
            if (values.size() == 1) {
                claims.setClaim(name, values.get(0));
            } else if (values.size() > 1) {
                claims.setClaim(name, values);
            }
        };
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
