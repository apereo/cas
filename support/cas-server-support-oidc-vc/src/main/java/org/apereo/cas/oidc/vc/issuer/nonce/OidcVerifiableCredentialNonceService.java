package org.apereo.cas.oidc.vc.issuer.nonce;

import module java.base;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link OidcVerifiableCredentialNonceService}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public interface OidcVerifiableCredentialNonceService {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "oidcVerifiableCredentialNonceService";

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
     * @return the count of removed nonces
     */
    int remove(@Nullable String nonce);

    /**
     * Exists nonce?.
     *
     * @param nonce the nonce
     * @return true/false
     */
    boolean exists(String nonce);

    /**
     * Atomically consume a nonce so that it can never be presented twice.
     *
     * @param nonce the nonce
     * @return true when this caller is the one that removed a valid nonce
     */
    boolean consume(@Nullable String nonce);

    record VerifiableCredentialNonce(String value, Long expiresIn) {
    }
}
