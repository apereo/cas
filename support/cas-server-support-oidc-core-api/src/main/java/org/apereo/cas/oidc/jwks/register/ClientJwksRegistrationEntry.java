package org.apereo.cas.oidc.jwks.register;

import module java.base;

/**
 * This is {@link ClientJwksRegistrationEntry}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public record ClientJwksRegistrationEntry(String jkt, String jwk, Instant createdAt) implements Serializable {
    @Serial
    private static final long serialVersionUID = -3122269588579072890L;
}
