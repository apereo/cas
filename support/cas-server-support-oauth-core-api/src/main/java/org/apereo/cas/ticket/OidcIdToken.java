package org.apereo.cas.ticket;

import org.jose4j.jwt.JwtClaims;

/**
 * This is {@link OidcIdToken}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public record OidcIdToken(String token, JwtClaims claims) {
    @Override
    public String toString() {
        return this.token;
    }
}
