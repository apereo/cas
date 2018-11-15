package org.apereo.cas.services;

/**
 * This is {@link ServiceRegistryExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface ServiceRegistryExecutionPlanConfigurer {
    /**
     * Configure service registry.
     *
     * @param plan the plan
     */
    default void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
    }
}
