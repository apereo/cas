package org.apereo.cas.services;

/**
 * This is {@link ServiceRegistryExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface ServiceRegistryExecutionPlanConfigurer {
    /**
     * Configure service registry.
     *
     * @param plan the plan
     */
    void configureServiceRegistry(ServiceRegistryExecutionPlan plan);

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
