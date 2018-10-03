package org.apereo.cas.authentication;

import java.util.Optional;

/**
 * A stateful authentication policy that is applied using arbitrary contextual information.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public interface ContextualAuthenticationPolicy<T> {
    /**
     * Gets the context used to evaluate the authentication policy.
     *
     * @return Context information.
     */
    T getContext();

    /**
     * Return an optional message code to use when this is unsatisfied.
     *
     * @return Optional message code
     */
    default Optional<String> getCode() {
        return Optional.empty();
    }

    /**
     * Determines whether an authentication event is satisfied by arbitrary security policy.
     *
     * @param authentication Authentication event to examine for compliance with security policy.
     * @return True if authentication isSatisfiedBy security policy, false otherwise.
     * @throws Exception the exception
     */
    boolean isSatisfiedBy(Authentication authentication) throws Exception;

}
