package org.apereo.cas.oidc.jwks.register;

import module java.base;

/**
 * This is {@link ClientJwksRegistrationRequest}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public record ClientJwksRegistrationRequest(String nonceId, String publicJwk, String signature) {
}
