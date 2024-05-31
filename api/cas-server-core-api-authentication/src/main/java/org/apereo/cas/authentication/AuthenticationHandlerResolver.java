package org.apereo.cas.authentication;

import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import java.util.LinkedHashSet;
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
    /**
     * Logger instance.
     */
    Logger LOGGER = LoggerFactory.getLogger(AuthenticationHandlerResolver.class);

    /**
     * NoOp authentication handler resolver.
     *
     * @return the authentication handler resolver
     */
    static AuthenticationHandlerResolver noOp() {
        return new AuthenticationHandlerResolver() {
            @Override
            public Set<AuthenticationHandler> resolve(final Set<AuthenticationHandler> candidateHandlers,
                                                      final AuthenticationTransaction transaction) {
                return new LinkedHashSet<>();
            }

            @Override
            public boolean supports(final Set<AuthenticationHandler> handlers,
                                    final AuthenticationTransaction transaction) {
                return false;
            }
        };
    }

    /**
     * Resolve set of authentication handlers.
     *
     * @param candidateHandlers the candidate handlers
     * @param transaction       the transaction
     * @return the set
     * @throws Throwable the throwable
     */
    default Set<AuthenticationHandler> resolve(final Set<AuthenticationHandler> candidateHandlers,
                                               final AuthenticationTransaction transaction) throws Throwable {
        val handlers = candidateHandlers
            .stream()
            .filter(handler -> handler.getState() == AuthenticationHandlerStates.ACTIVE)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        LOGGER.debug("Default authentication handlers used for this transaction are [{}]",
            handlers.stream().map(AuthenticationHandler::getName).collect(Collectors.joining(",")));
        return handlers;
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
     * @return true /false
     * @throws Throwable the throwable
     */
    default boolean supports(final Set<AuthenticationHandler> handlers, final AuthenticationTransaction transaction) throws Throwable {
        return !handlers.isEmpty() && transaction != null;
    }
}
