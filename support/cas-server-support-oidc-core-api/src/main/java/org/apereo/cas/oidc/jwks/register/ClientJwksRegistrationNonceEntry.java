package org.apereo.cas.oidc.jwks.register;

import module java.base;

/**
 * This is {@link ClientJwksRegistrationNonceEntry}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public record ClientJwksRegistrationNonceEntry(String id, String nonce, Instant expiration) {
}
