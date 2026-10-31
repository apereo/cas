package org.apereo.cas.services.replication;

import module java.base;
import org.apereo.cas.configuration.model.support.services.stream.StreamingServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.services.stream.StreamingServicesCoreProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.DisposableBean;

/**
 * This is {@link DefaultRegisteredServiceReplicationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultRegisteredServiceReplicationStrategy implements RegisteredServiceReplicationStrategy, DisposableBean {
    private final DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>, PublisherIdentifier> distributedCacheManager;

    private final StreamingServiceRegistryProperties properties;

    private final PublisherIdentifier publisherIdentifier;

    private static boolean isRegisteredServiceMarkedAsDeletedInCache(final DistributedCacheObject<RegisteredService> item) {
        if (item.containsProperty("event")) {
            val event = item.getProperty("event", String.class);
            return event.equalsIgnoreCase(CasRegisteredServiceDeletedEvent.class.getSimpleName());
        }
        return false;
    }

    @Override
    public void destroy() {
        FunctionUtils.doIfNotNull(distributedCacheManager, DistributedCacheManager::close);
    }

    @Override
    public RegisteredService getRegisteredServiceFromCacheIfAny(final RegisteredService service, final String id,
                                                                final ServiceRegistry serviceRegistry) {
        return getRegisteredServiceFromCacheByPredicate(service, value -> value.getValue().matches(id), serviceRegistry);
    }

    @Override
    public RegisteredService getRegisteredServiceFromCacheIfAny(final RegisteredService service, final long id,
                                                                final ServiceRegistry serviceRegistry) {
        return getRegisteredServiceFromCacheByPredicate(service, value -> value.getValue().getId() == id, serviceRegistry);
    }

    /**
     * Reconcile the local service with the replication cache. A deletion marker leaves the local service
     * alone: lookups also run while a service is being saved or picked up by the file watcher, and acting
     * on the marker there would delete the definition being added. Deletions are applied when services load.
     *
     * @param service         the service found locally, if any
     * @param predicate       the predicate that selects the cache entry
     * @param serviceRegistry the service registry of this node
     * @return the service to use, or null if there is none
     */
    @Override
    public @Nullable RegisteredService getRegisteredServiceFromCacheByPredicate(
        final RegisteredService service,
        final Predicate<DistributedCacheObject<RegisteredService>> predicate,
        final ServiceRegistry serviceRegistry) {
        val result = this.distributedCacheManager.find(predicate);
        if (result.isPresent()) {
            val item = result.get();
            val value = item.getValue();
            LOGGER.debug("Located cache entry [{}] in service registry cache [{}]", item, this.distributedCacheManager.getName());
            if (isRegisteredServiceMarkedAsDeletedInCache(item)) {
                LOGGER.debug("Service found in the cache [{}] is marked as a deleted service; the local service is removed on the next load", value);
                return service;
            }

            if (service == null) {
                LOGGER.debug("Service is in not found in the local service registry for this CAS node. CAS will use the cache entry [{}] instead "
                             + "and will update the service registry of this CAS node with the cache entry for future look-ups", value);
                saveRegisteredServiceIfNecessary(serviceRegistry, value);
                return value;
            }
            LOGGER.debug("Service definition cache entry [{}] carries the timestamp [{}]", value, item.getTimestamp());
            if (value.equals(service)) {
                LOGGER.debug("Service definition cache entry is the same as service definition found locally");
                return service;
            }
            LOGGER.debug("Service definition found in the cache [{}] is more recent than its counterpart on this CAS node. CAS will "
                         + "use the cache entry and update the service registry of this CAS node with the cache entry for future look-ups", value);

            saveRegisteredServiceIfNecessary(serviceRegistry, value);

            return value;
        }
        LOGGER.debug("Requested service definition is not found in the replication cache");
        if (service != null) {
            LOGGER.debug("Attempting to update replication cache with service [{}]", service);
            val item = DistributedCacheObject.<RegisteredService>builder()
                .value(service)
                .publisherIdentifier(publisherIdentifier)
                .build();
            this.distributedCacheManager.set(service, item, true);
        }
        return service;
    }

    /**
     * Apply the cached services to the services loaded on this node. A cached service replaces the loaded
     * service with the same id rather than being listed next to it. Older cache entries for the same id,
     * left behind when a service is renamed, are removed. The cache is otherwise kept after the load,
     * so that nodes started later receive every service and loaded services that the cache already holds
     * are not sent again. A deleted service stays in the cache as a deletion marker, so that every node,
     * including one started later, removes its local copy instead of sending it back; the marker is
     * replaced only when a service with that id is saved again.
     *
     * @param services        the services loaded on this node
     * @param serviceRegistry the service registry of this node
     * @return the services
     */
    @Override
    public List<RegisteredService> updateLoadedRegisteredServicesFromCache(final List<RegisteredService> services,
                                                                           final ServiceRegistry serviceRegistry) {
        val allCachedServices = distributedCacheManager.getAll()
            .stream()
            .sorted(Comparator.<DistributedCacheObject>comparingLong(DistributedCacheObject::getTimestamp).reversed())
            .toList();
        val cachedServices = allCachedServices
            .stream()
            .filter(CollectionUtils.distinctByKey(service -> service.getValue().getId()))
            .toList();
        allCachedServices
            .stream()
            .filter(entry -> !cachedServices.contains(entry))
            .forEach(entry -> distributedCacheManager.remove(entry.getValue(), entry, false));
        for (val entry : cachedServices) {
            val cachedService = entry.getValue();
            LOGGER.debug("Found cached service definition [{}] in the replication cache [{}]",
                cachedService, distributedCacheManager.getName());

            if (isRegisteredServiceMarkedAsDeletedInCache(entry)) {
                services.stream()
                    .filter(svc -> svc.getId() == cachedService.getId())
                    .findFirst()
                    .ifPresent(localService -> {
                        LOGGER.debug("Service found in the cache [{}] is marked as a deleted service. CAS will update the service registry "
                                     + "of this CAS node to remove the local service", cachedService);
                        serviceRegistry.delete(localService);
                        services.remove(localService);
                    });
                continue;
            }

            val matchingService = services.stream()
                .filter(svc -> svc.getId() == cachedService.getId())
                .findFirst()
                .orElse(null);

            if (matchingService != null) {
                updateServiceRegistryWithMatchingService(services, cachedService, matchingService, serviceRegistry);
            } else {
                updateServiceRegistryWithNoMatchingService(services, cachedService, serviceRegistry);
            }
        }
        return services;
    }

    private void saveRegisteredServiceIfNecessary(final ServiceRegistry serviceRegistry, final RegisteredService value) {
        if (properties.getCore().getReplicationMode() == StreamingServicesCoreProperties.ReplicationModes.ACTIVE) {
            serviceRegistry.save(value);
        }
    }

    private void updateServiceRegistryWithNoMatchingService(final List<RegisteredService> services,
                                                            final RegisteredService cachedService,
                                                            final ServiceRegistry serviceRegistry) {
        LOGGER.debug("No corresponding service definition could be matched against cache entry [{}] locally. "
                     + "CAS will update the service registry of this CAS node with the cache entry for future look-ups", cachedService);
        updateServiceRegistryWithRegisteredService(services, cachedService, serviceRegistry);
    }

    private void updateServiceRegistryWithMatchingService(final List<RegisteredService> services,
                                                          final RegisteredService cachedService,
                                                          final RegisteredService matchingService,
                                                          final ServiceRegistry serviceRegistry) {
        LOGGER.debug("Found corresponding service definition [{}] locally via cache manager [{}]", matchingService, distributedCacheManager.getName());
        if (matchingService.equals(cachedService)) {
            LOGGER.debug("Service definition cache entry [{}] is the same as service definition found locally [{}]", cachedService, matchingService);
        } else {
            LOGGER.debug("Service definition found in the cache [{}] is more recent than its counterpart on this CAS node. "
                         + "CAS will update the service registry of this CAS node with the cache entry for future look-ups", cachedService);
            saveRegisteredServiceIfNecessary(serviceRegistry, cachedService);
            services.set(services.indexOf(matchingService), cachedService);
        }
    }

    private void updateServiceRegistryWithRegisteredService(final List<RegisteredService> services,
                                                            final RegisteredService cachedService,
                                                            final ServiceRegistry serviceRegistry) {
        saveRegisteredServiceIfNecessary(serviceRegistry, cachedService);
        services.add(cachedService);
    }
}
