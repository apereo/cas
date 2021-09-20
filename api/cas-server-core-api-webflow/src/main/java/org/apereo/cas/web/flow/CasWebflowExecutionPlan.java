package org.apereo.cas.web.flow;

import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collection;

/**
 * This is {@link CasWebflowExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface CasWebflowExecutionPlan {

    /**
     * Implementation bean name.
     */
    String BEAN_NAME = "casWebflowExecutionPlan";

    /**
     * Register webflow login context provider.
     *
     * @param provider the provider
     */
    void registerWebflowLoginContextProvider(CasWebflowLoginContextProvider provider);

    /**
     * Register webflow configurer.
     *
     * @param cfg the cfg
     */
    void registerWebflowConfigurer(CasWebflowConfigurer cfg);

    /**
     * Register webflow interceptor.
     *
     * @param interceptor the interceptor
     */
    void registerWebflowInterceptor(HandlerInterceptor interceptor);

    /**
     * Gets webflow configurers.
     *
     * @return the webflow configurers
     */
    Collection<CasWebflowConfigurer> getWebflowConfigurers();

    /**
     * Gets webflow interceptors.
     *
     * @return the webflow interceptors
     */
    Collection<HandlerInterceptor> getWebflowInterceptors();

    /**
     * Gets webflow login context providers.
     *
     * @return the webflow login context providers
     */
    Collection<CasWebflowLoginContextProvider> getWebflowLoginContextProviders();

    /**
     * Execute the plan.
     */
    void execute();
}
