package org.apereo.cas.oidc.vc.issuer.nonce;

import module java.base;

/**
 * This is {@link OidcVerifiableCredentialNonceService}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public interface OidcVerifiableCredentialNonceService {
    /**
     * Create verifiable credential nonce.
     *
     * @return the verifiable credential nonce
     */
    VerifiableCredentialNonce create();

    /**
     * Consume nonce.
     *
     * @param nonce the nonce
     */
    void remove(String nonce);

    /**
     * Exists nonce?.
     *
     * @param nonce the nonce
     * @return true/false
     */
    boolean exists(String nonce);

    record VerifiableCredentialNonce(String value, Instant expiresAt) {
    }
}
