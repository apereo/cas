package org.apereo.cas.authentication;

import java.util.Optional;

/**
 * A stateful authentication policy that is applied using arbitrary contextual information.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public interface ContextualAuthenticationPolicy<T> extends AuthenticationPolicy {
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
}
