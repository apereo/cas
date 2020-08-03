package org.apereo.cas.services;

import java.util.Collection;
import java.util.List;

/**
 * This is {@link ChainingServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public interface ChainingServiceRegistry extends ServiceRegistry {

    /**
     * Count registries in the chain.
     *
     * @return the count
     */
    long countServiceRegistries();

    /**
     * Add service registry.
     *
     * @param registry the registry
     */
    default void addServiceRegistry(final ServiceRegistry registry) {
        addServiceRegistries(List.of(registry));
    }

    /**
     * Add service registries.
     *
     * @param registries the registries
     */
    void addServiceRegistries(Collection<ServiceRegistry> registries);

    /**
     * Gets service registries.
     *
     * @return the service registries
     */
    List<ServiceRegistry> getServiceRegistries();

    /**
     * Synchronize the service definition across all registries in the chain.
     *
     * @param service the service
     */
    void synchronize(RegisteredService service);
}
