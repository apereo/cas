package org.apereo.cas.services;

import java.util.Collection;
import java.util.function.Predicate;

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
    ServiceRegistryExecutionPlan registerServiceRegistry(ServiceRegistry registry);

    /**
     * Get service registries collection.
     *
     * @return the collection
     */
    Collection<ServiceRegistry> getServiceRegistries();

    /**
     * Get service registries collection.
     *
     * @param typeFilter the type filter
     * @return the collection
     */
    Collection<ServiceRegistry> find(Predicate<ServiceRegistry> typeFilter);

}
