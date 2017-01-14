package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
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
        if (principalResolver == null) {
            LOGGER.debug("Registering handler [{}] with no principal resolver into the execution plan", handler.getName(), principalResolver);
        } else {
            LOGGER.debug("Registering handler [{}] principal resolver [{}] into the execution plan", handler.getName(), principalResolver);
        }
    }

    @Override
    public void registerAuthenticationHandlerWithPrincipalResolver(final Map<AuthenticationHandler, PrincipalResolver> plan) {
        plan.forEach((k, v) -> registerAuthenticationHandlerWithPrincipalResolver(k, v));
    }

    @Override
    public void registerMetadataPopulator(final AuthenticationMetaDataPopulator populator) {
        LOGGER.debug("Registering metadata populator [{}] into the execution plan", populator);
    }

    @Override
    public void registerMetadataPopulators(final Collection<AuthenticationMetaDataPopulator> populators) {
        populators.forEach(p -> registerMetadataPopulator(p));
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

    @Override
    public Collection<AuthenticationMetaDataPopulator> getAuthenticationMetadataPopulators(final Collection<Credential> credentials) {
        return null;
    }
}
