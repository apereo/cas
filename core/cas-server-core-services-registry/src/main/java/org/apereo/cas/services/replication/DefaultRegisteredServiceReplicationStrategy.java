package org.apereo.cas.services.replication;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.DistributedCacheManager;
import org.apereo.cas.DistributedCacheObject;
import org.apereo.cas.configuration.model.support.services.stream.StreamingServiceRegistryProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.support.events.service.BaseCasRegisteredServiceEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;

import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * This is {@link DefaultRegisteredServiceReplicationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@AllArgsConstructor
public class DefaultRegisteredServiceReplicationStrategy implements RegisteredServiceReplicationStrategy {
    private final DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>> distributedCacheManager;
    private final StreamingServiceRegistryProperties properties;
    
    /**
     * Destroy the watch service thread.
     *
     * @throws Exception the exception
     */
    @PreDestroy
    public void destroy() throws Exception {
        if (this.distributedCacheManager != null) {
            this.distributedCacheManager.close();
        }
    }

    @Override
    public RegisteredService getRegisteredServiceFromCacheIfAny(final RegisteredService service, final String id,
                                                                final ServiceRegistryDao serviceRegistryDao) {
        return getRegisteredServiceFromCacheByPredicate(service, value -> value.getValue().matches(id), serviceRegistryDao);
    }

    @Override
    public RegisteredService getRegisteredServiceFromCacheIfAny(final RegisteredService service, final long id,
                                                                final ServiceRegistryDao serviceRegistryDao) {
        return getRegisteredServiceFromCacheByPredicate(service, value -> value.getValue().getId() == id, serviceRegistryDao);
    }

    @Override
    public RegisteredService getRegisteredServiceFromCacheByPredicate(final RegisteredService service,
                                                                      final Predicate<DistributedCacheObject<RegisteredService>> predicate,
                                                                      final ServiceRegistryDao serviceRegistryDao) {
        final Optional<DistributedCacheObject<RegisteredService>> result = this.distributedCacheManager.find(predicate);
        if (result.isPresent()) {
            final DistributedCacheObject<RegisteredService> item = result.get();
            final RegisteredService cachedService = item.getValue();
            LOGGER.debug("Located cache entry [{}] in service registry cache [{}]", item, this.distributedCacheManager.getName());
            if (isRegisteredServiceMarkedAsDeletedInCache(item)) {
                LOGGER.debug("Service found in the cache [{}] is marked as a deleted service. CAS will update the service registry "
                    + "of this CAS node to remove the local service, if found", cachedService);
                serviceRegistryDao.delete(cachedService);
                this.distributedCacheManager.remove(cachedService, item);
                return service;
            }

            if (service == null) {

                LOGGER.debug("Service is in not found in the local service registry for this CAS node. CAS will use the cache entry [{}] instead "
                    + "and will update the service registry of this CAS node with the cache entry for future look-ups", item.getValue());
                if (properties.getReplicationMode() == StreamingServiceRegistryProperties.ReplicationModes.ACTIVE_ACTIVE) {
                    serviceRegistryDao.save(item.getValue());
                }
                return item.getValue();
            }
            LOGGER.debug("Service definition cache entry [{}] carries the timestamp [{}]", item.getValue(), item.getTimestamp());
            if (item.getValue().equals(service)) {
                LOGGER.debug("Service definition cache entry is the same as service definition found locally");
                return service;
            }
            LOGGER.debug("Service definition found in the cache [{}] is more recent than its counterpart on this CAS node. CAS will "
                + "use the cache entry and update the service registry of this CAS node with the cache entry for future look-ups", item.getValue());

            if (properties.getReplicationMode() == StreamingServiceRegistryProperties.ReplicationModes.ACTIVE_ACTIVE) {
                serviceRegistryDao.save(item.getValue());
            }
            
            return item.getValue();
        }
        LOGGER.debug("Requested service definition is not found in the replication cache");
        if (service != null) {
            LOGGER.debug("Attempting to update replication cache with service [{}}", service);
            final DistributedCacheObject<RegisteredService> item = new DistributedCacheObject<>(service);
            this.distributedCacheManager.set(service, item);
        }
        return service;
    }

    @Override
    public List<RegisteredService> updateLoadedRegisteredServicesFromCache(final List<RegisteredService> services,
                                                                           final ServiceRegistryDao serviceRegistryDao) {
        final Collection<DistributedCacheObject<RegisteredService>> cachedServices = this.distributedCacheManager.getAll();

        for (final DistributedCacheObject<RegisteredService> entry : cachedServices) {
            final RegisteredService cachedService = entry.getValue();
            LOGGER.debug("Found cached service definition [{}] in the replication cache [{}]", cachedService, distributedCacheManager.getName());

            if (isRegisteredServiceMarkedAsDeletedInCache(entry)) {
                LOGGER.debug("Service found in the cache [{}] is marked as a deleted service. CAS will update the service registry "
                    + "of this CAS node to remove the local service, if found.", cachedService);
                serviceRegistryDao.delete(cachedService);
                this.distributedCacheManager.remove(cachedService, entry);
                continue;
            }

            final RegisteredService matchingService = services.stream()
                .filter(s -> s.getId() == cachedService.getId())
                .findFirst()
                .orElse(null);
            if (matchingService != null) {
                updateServiceRegistryWithMatchingService(services, cachedService, matchingService, serviceRegistryDao);
            } else {
                updateServiceRegistryWithNoMatchingService(services, cachedService, serviceRegistryDao);
            }
        }
        return services;
    }

    private void updateServiceRegistryWithNoMatchingService(final List<RegisteredService> services, final RegisteredService cachedService,
                                                            final ServiceRegistryDao serviceRegistryDao) {
        LOGGER.debug("No corresponding service definition could be matched against cache entry [{}] locally. "
            + "CAS will update the service registry of this CAS node with the cache entry for future look-ups", cachedService);
        updateServiceRegistryWithRegisteredService(services, cachedService, serviceRegistryDao);
    }

    private void updateServiceRegistryWithMatchingService(final List<RegisteredService> services, final RegisteredService cachedService,
                                                          final RegisteredService matchingService, final ServiceRegistryDao serviceRegistryDao) {
        LOGGER.debug("Found corresponding service definition [{}] locally via cache manager [{}]", matchingService, distributedCacheManager.getName());
        if (matchingService.equals(cachedService)) {
            LOGGER.debug("Service definition cache entry [{}] is the same as service definition found locally [{}]", cachedService, matchingService);
        } else {
            LOGGER.debug("Service definition found in the cache [{}] is more recent than its counterpart on this CAS node. "
                + "CAS will update the service registry of this CAS node with the cache entry for future look-ups", cachedService);
            updateServiceRegistryWithRegisteredService(services, cachedService, serviceRegistryDao);
        }
    }

    private void updateServiceRegistryWithRegisteredService(final List<RegisteredService> services, final RegisteredService cachedService,
                                                            final ServiceRegistryDao serviceRegistryDao) {
        if (properties.getReplicationMode() == StreamingServiceRegistryProperties.ReplicationModes.ACTIVE_ACTIVE) {
            serviceRegistryDao.save(cachedService);
        }
        services.add(cachedService);
    }

    private boolean isRegisteredServiceMarkedAsDeletedInCache(final DistributedCacheObject<RegisteredService> item) {
        if (item.containsProperty("event")) {
            final BaseCasRegisteredServiceEvent event = item.getProperty("event", BaseCasRegisteredServiceEvent.class);
            return event instanceof CasRegisteredServiceDeletedEvent;
        }
        return false;
    }
}
