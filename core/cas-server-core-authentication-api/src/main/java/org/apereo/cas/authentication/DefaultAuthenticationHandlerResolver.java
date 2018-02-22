package org.apereo.cas.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultAuthenticationHandlerResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class DefaultAuthenticationHandlerResolver implements AuthenticationHandlerResolver {
    @Override
    public Set<AuthenticationHandler> resolve(final Set<AuthenticationHandler> candidateHandlers, final AuthenticationTransaction transaction) {
        final String handlers = candidateHandlers.stream().map(AuthenticationHandler::getName).collect(Collectors.joining(","));
        LOGGER.debug("Authentication handlers used for this transaction are [{}]", handlers);
        return candidateHandlers;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
