package org.apereo.cas.oidc.jwks.register;

import module java.base;
import org.springframework.data.annotation.Id;

/**
 * This is {@link ClientJwksRegistrationEntry}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public record ClientJwksRegistrationEntry(
    @Id String jkt, String jwk, Instant createdAt) implements Serializable {
    @Serial
    private static final long serialVersionUID = -3122269588579072890L;
}
