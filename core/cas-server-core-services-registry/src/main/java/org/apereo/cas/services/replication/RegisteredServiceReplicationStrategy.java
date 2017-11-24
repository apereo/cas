package org.apereo.cas.services.replication;

import org.apereo.cas.DistributedCacheObject;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistryDao;

import java.util.List;
import java.util.function.Predicate;

/**
 * This is {@link RegisteredServiceReplicationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface RegisteredServiceReplicationStrategy {
    /**
     * Gets registered service from cache if any.
     *
     * @param service            the service
     * @param id                 the id
     * @param serviceRegistryDao the service registry dao
     * @return the registered service from cache if any
     */
    default RegisteredService getRegisteredServiceFromCacheIfAny(final RegisteredService service, final String id,
                                                                 final ServiceRegistryDao serviceRegistryDao) {
        return service;
    }

    /**
     * Gets registered service from cache if any.
     *
     * @param service            the service
     * @param id                 the id
     * @param serviceRegistryDao the service registry dao
     * @return the registered service from cache if any
     */
    default RegisteredService getRegisteredServiceFromCacheIfAny(final RegisteredService service, final long id,
                                                                 final ServiceRegistryDao serviceRegistryDao) {
        return service;
    }

    /**
     * Gets registered service from cache by predicate.
     *
     * @param service            the service
     * @param predicate          the predicate
     * @param serviceRegistryDao the service registry dao
     * @return the registered service from cache by predicate
     */
    default RegisteredService getRegisteredServiceFromCacheByPredicate(final RegisteredService service,
                                                                       final Predicate<DistributedCacheObject<RegisteredService>> predicate,
                                                                       final ServiceRegistryDao serviceRegistryDao) {
        return service;
    }

    /**
     * Update loaded registered services from cache list.
     *
     * @param services           the services
     * @param serviceRegistryDao the service registry dao
     * @return the list
     */
    default List<RegisteredService> updateLoadedRegisteredServicesFromCache(final List<RegisteredService> services,
                                                                            final ServiceRegistryDao serviceRegistryDao) {
        return services;
    }
}
