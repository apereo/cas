package org.apereo.cas.authentication;

import java.util.Set;

/**
 * This is {@link AuthenticationHandlerResolver} which decides which set of
 * authentication handlers shall be chosen for a given authN event.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface AuthenticationHandlerResolver {

    /**
     * Resolve set of authentication handlers.
     *
     * @param candidateHandlers the candidate handlers
     * @param transaction       the transaction
     * @return the set
     */
    Set<AuthenticationHandler> resolve(Set<AuthenticationHandler> candidateHandlers,
                                       AuthenticationTransaction transaction);
}
