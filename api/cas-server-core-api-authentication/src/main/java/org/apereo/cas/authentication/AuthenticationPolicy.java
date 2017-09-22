package org.apereo.cas.authentication;

import org.springframework.core.Ordered;

/**
 * Strategy interface for pluggable authentication security policies.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public interface AuthenticationPolicy extends Ordered {
    /**
     * Determines whether an authentication event isSatisfiedBy arbitrary security policy.
     *
     * @param authentication Authentication event to examine for compliance with security policy.
     * @return True if authentication isSatisfiedBy security policy, false otherwise.
     */
    boolean isSatisfiedBy(Authentication authentication);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
