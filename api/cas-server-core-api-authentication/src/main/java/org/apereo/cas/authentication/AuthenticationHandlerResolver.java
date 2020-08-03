package org.apereo.cas.authentication;

import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link AuthenticationHandlerResolver} which decides which set of
 * authentication handlers shall be chosen for a given authN event.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface AuthenticationHandlerResolver extends Ordered {
    Logger LOGGER = LoggerFactory.getLogger(AuthenticationHandlerResolver.class);

    /**
     * Resolve set of authentication handlers.
     *
     * @param candidateHandlers the candidate handlers
     * @param transaction       the transaction
     * @return the set
     */
    default Set<AuthenticationHandler> resolve(final Set<AuthenticationHandler> candidateHandlers, final AuthenticationTransaction transaction) {
        val handlers = candidateHandlers.stream().map(AuthenticationHandler::getName).collect(Collectors.joining(","));
        LOGGER.debug("Default authentication handlers used for this transaction are [{}]", handlers);
        return candidateHandlers;
    }

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Supports this transaction?
     *
     * @param handlers    the handlers
     * @param transaction the transaction
     * @return true/false
     */
    default boolean supports(final Set<AuthenticationHandler> handlers, final AuthenticationTransaction transaction) {
        return !handlers.isEmpty() && transaction != null;
    }
}
