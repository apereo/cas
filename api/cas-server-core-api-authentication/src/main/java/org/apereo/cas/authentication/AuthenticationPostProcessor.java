package org.apereo.cas.authentication;

import org.springframework.core.Ordered;

/**
 * This is {@link AuthenticationPostProcessor}.
 * Authentication post processors are immediately after the authentication
 * transaction has complete successfully. The main difference is that post-processors
 * have access to the original authentication transaction and also get a chance to
 * update the authentication object with additional metadata and attributes, etc.
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface AuthenticationPostProcessor extends Ordered {
    /**
     * Process.
     *
     * @param transaction the transaction
     * @param builder     the builder
     */
    void process(AuthenticationTransaction transaction, AuthenticationBuilder builder);
}
