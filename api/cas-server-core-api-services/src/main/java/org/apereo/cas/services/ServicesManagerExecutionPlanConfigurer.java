package org.apereo.cas.services;

/**
 * This is {@link ServicesManagerExecutionPlanConfigurer}.
 *
 * @author Travis Schmidt
 * @since 6.1.0
 */
public interface ServicesManagerExecutionPlanConfigurer {
    /**
     * Configure services manager.
     *
     * @param plan the plan
     */
    default void configureServicesManager(final ServicesManagerExecutionPlan plan) {
    }
}
