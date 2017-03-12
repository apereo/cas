package org.apereo.cas.authentication;

import org.springframework.core.Ordered;

/**
 * This is {@link AuthenticationPostProcessor}.
 *
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
