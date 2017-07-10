package org.apereo.cas.authentication;

/**
 * A factory for producing (stateful) authentication policies based on arbitrary context data.
 * This component provides a way to inject stateless factories into components that produce stateful
 * authentication policies that can leverage arbitrary contextual information to evaluate security policy.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@FunctionalInterface
public interface ContextualAuthenticationPolicyFactory<T> {

    /**
     * Creates a contextual (presumably stateful) authentication policy based on provided context data.
     *
     * @param context Context data used to create an authentication policy.
     *
     * @return Contextual authentication policy object. The returned object should be assumed to be stateful
     * and not thread safe unless explicitly noted otherwise.
     */
    ContextualAuthenticationPolicy<T> createPolicy(T context);
}
