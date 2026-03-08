package org.apereo.cas.oidc.jwks.register;

import module java.base;

/**
 * This is {@link ClientJwksRegistrationNonceStore}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public interface ClientJwksRegistrationNonceStore {
    /**
     * Create client jwks challenge response.
     *
     * @return the client jwks challenge response
     */
    ClientJwksChallengeResponse create();

    /**
     * Find client jwks registration nonce entry.
     *
     * @param nonceId the nonce id
     * @return the client jwks registration nonce entry
     */
    ClientJwksRegistrationNonceEntry find(String nonceId);

    void remove(String nonceId);
}
