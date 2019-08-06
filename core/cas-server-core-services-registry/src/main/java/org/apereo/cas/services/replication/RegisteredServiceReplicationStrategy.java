package org.apereo.cas.services.replication;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.util.cache.DistributedCacheObject;

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
     * @param service         the service
     * @param id              the id
     * @param serviceRegistry the service registry dao
     * @return the registered service from cache if any
     */
    default RegisteredService getRegisteredServiceFromCacheIfAny(final RegisteredService service, final String id,
                                                                 final ServiceRegistry serviceRegistry) {
        return service;
    }

    /**
     * Gets registered service from cache if any.
     *
     * @param service         the service
     * @param id              the id
     * @param serviceRegistry the service registry dao
     * @return the registered service from cache if any
     */
    default RegisteredService getRegisteredServiceFromCacheIfAny(final RegisteredService service, final long id,
                                                                 final ServiceRegistry serviceRegistry) {
        return service;
    }

    /**
     * Gets registered service from cache by predicate.
     *
     * @param service         the service
     * @param predicate       the predicate
     * @param serviceRegistry the service registry dao
     * @return the registered service from cache by predicate
     */
    default RegisteredService getRegisteredServiceFromCacheByPredicate(final RegisteredService service,
                                                                       final Predicate<DistributedCacheObject<RegisteredService>> predicate,
                                                                       final ServiceRegistry serviceRegistry) {
        return service;
    }

    /**
     * Update loaded registered services from cache list.
     *
     * @param services        the services
     * @param serviceRegistry the service registry dao
     * @return the list
     */
    default List<RegisteredService> updateLoadedRegisteredServicesFromCache(final List<RegisteredService> services,
                                                                            final ServiceRegistry serviceRegistry) {
        return services;
    }
}
