package org.apereo.cas.util.crypto;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link CipherExecutorResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@FunctionalInterface
public interface CipherExecutorResolver {

    /**
     * Resolve cipher executor.
     *
     * @param request the request
     * @return the cipher executor
     */
    CipherExecutor resolve(HttpServletRequest request);

    /**
     * Static cipher executor cipher.
     *
     * @param cipherExecutor the cipher executor
     * @return the cipher executor resolver
     */
    static CipherExecutorResolver withCipherExecutor(final CipherExecutor cipherExecutor) {
        return request -> cipherExecutor;
    }
}
