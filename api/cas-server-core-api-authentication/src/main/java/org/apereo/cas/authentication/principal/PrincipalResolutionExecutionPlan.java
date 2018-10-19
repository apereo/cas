package org.apereo.cas.authentication.principal;

import java.util.Collection;

/**
 * This is {@link PrincipalResolutionExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public interface PrincipalResolutionExecutionPlan {

    /**
     * Register principal resolver.
     *
     * @param principalResolver the principal resolver
     */
    void registerPrincipalResolver(PrincipalResolver principalResolver);

    /**
     * Gets registered principal resolvers.
     *
     * @return the registered principal resolvers
     */
    Collection<PrincipalResolver> getRegisteredPrincipalResolvers();
}
