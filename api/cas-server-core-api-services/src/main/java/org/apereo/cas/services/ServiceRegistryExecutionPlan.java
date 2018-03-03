package org.apereo.cas.services;

import java.util.Collection;

/**
 * This is {@link ServiceRegistryExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface ServiceRegistryExecutionPlan {
    /**
     * Register service registry.
     *
     * @param registry the registry
     * @return the service registry execution plan
     */
    ServiceRegistryExecutionPlan registerServiceRegistry(ServiceRegistryDao registry);

    /**
     * Get service registries collection.
     *
     * @return the collection
     */
    Collection<ServiceRegistryDao> getServiceRegistries();
}
