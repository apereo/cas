package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * This is {@link DefaultAuthenticationEventExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultAuthenticationEventExecutionPlan implements AuthenticationEventExecutionPlan {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthenticationEventExecutionPlan.class);

    @Override
    public void registerAuthenticationHandler(final AuthenticationHandler handler) {
        registerAuthenticationHandlerWithPrincipalResolver(handler, null);
    }

    @Override
    public void registerAuthenticationHandlerWithPrincipalResolver(final AuthenticationHandler handler, final PrincipalResolver principalResolver) {
        LOGGER.debug("Registering handler [{}] with resolver [{}] into the execution plan", handler, principalResolver);
    }

    @Override
    public void registerAuthenticationHandlerWithPrincipalResolver(final Map<AuthenticationHandler, PrincipalResolver> plan) {
        plan.forEach((k, v) -> registerAuthenticationHandlerWithPrincipalResolver(k, v));
    }

    @Override
    public Set<AuthenticationHandler> getAuthenticationHandlersForTransaction(final AuthenticationTransaction transaction) {
        return null;
    }

    @Override
    public PrincipalResolver getPrincipalResolverForAuthenticationTransaction(final AuthenticationHandler handler,
                                                                              final AuthenticationTransaction transaction) {
        return null;
    }
}
