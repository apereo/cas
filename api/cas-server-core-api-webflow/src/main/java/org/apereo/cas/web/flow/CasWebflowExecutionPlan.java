package org.apereo.cas.web.flow;

import java.util.Collection;

/**
 * This is {@link CasWebflowExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface CasWebflowExecutionPlan {

    /**
     * Register webflow configurer.
     *
     * @param cfg the cfg
     */
    void registerWebflowConfigurer(CasWebflowConfigurer cfg);

    /**
     * Gets webflow configurers.
     *
     * @return the webflow configurers
     */
    Collection<CasWebflowConfigurer> getWebflowConfigurers();

    /**
     * Execute the plan.
     */
    void execute();
}
