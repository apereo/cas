package org.apereo.cas.oidc.jwks.register;

import module java.base;

/**
 * This is {@link ClientJwksChallengeResponse}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public record ClientJwksChallengeResponse(String nonceId, String nonce) {
}
