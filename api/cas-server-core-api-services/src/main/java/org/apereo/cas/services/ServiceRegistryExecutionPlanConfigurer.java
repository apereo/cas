package org.apereo.cas.services;

import org.apereo.cas.util.NamedObject;

/**
 * This is {@link ServiceRegistryExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface ServiceRegistryExecutionPlanConfigurer extends NamedObject {
    /**
     * Configure service registry.
     *
     * @param plan the plan
     */
    void configureServiceRegistry(ServiceRegistryExecutionPlan plan);
    
}
