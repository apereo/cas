package org.apereo.cas.util.crypto;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link CipherExecutorResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public interface CipherExecutorResolver {

    /**
     * Resolve cipher executor.
     *
     * @param request the request
     * @return the cipher executor
     */
    CipherExecutor resolve(HttpServletRequest request);

    /**
     * Resolve cipher executor.
     *
     * @param tenant the tenant
     * @return the cipher executor
     */
    CipherExecutor resolve(String tenant);

    /**
     * Static cipher executor cipher.
     *
     * @param cipherExecutor the cipher executor
     * @return the cipher executor resolver
     */
    static CipherExecutorResolver with(final CipherExecutor cipherExecutor) {
        return new CipherExecutorResolver() {
            @Override
            public CipherExecutor resolve(final HttpServletRequest request) {
                return cipherExecutor;
            }

            @Override
            public CipherExecutor resolve(final String tenant) {
                return cipherExecutor;
            }
        };
    }
}
