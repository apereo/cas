package org.jasig.cas.authentication;

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
}
