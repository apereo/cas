package org.apereo.cas.authentication;

import org.springframework.core.Ordered;

import java.util.Set;

/**
 * This is {@link AuthenticationPolicyResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public interface AuthenticationPolicyResolver extends Ordered {
    /**
     * Resolve set of authentication handlers.
     *
     * @param transaction the transaction
     * @return the set
     */
    Set<AuthenticationPolicy> resolve(AuthenticationTransaction transaction);

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Supports this transaction?
     *
     * @param transaction the transaction
     * @return true/false
     */
    default boolean supports(final AuthenticationTransaction transaction) {
        return transaction != null;
    }
}
