package org.apereo.cas.services.replication;

import org.apereo.cas.configuration.model.support.services.stream.StreamingServiceRegistryProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;

import java.util.List;
import java.util.function.Predicate;

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

    @Override
    public void destroy() {
        if (this.distributedCacheManager != null) {
            this.distributedCacheManager.close();
        }
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

    @Override
    public RegisteredService getRegisteredServiceFromCacheByPredicate(final RegisteredService service,
                                                                      final Predicate<DistributedCacheObject<RegisteredService>> predicate,
                                                                      final ServiceRegistry serviceRegistry) {
        val result = this.distributedCacheManager.find(predicate);
        if (result.isPresent()) {
            val item = result.get();
            val value = item.getValue();
            LOGGER.debug("Located cache entry [{}] in service registry cache [{}]", item, this.distributedCacheManager.getName());
            if (isRegisteredServiceMarkedAsDeletedInCache(item)) {
                LOGGER.debug("Service found in the cache [{}] is marked as a deleted service. CAS will update the service registry "
                    + "of this CAS node to remove the local service, if found", value);
                serviceRegistry.delete(value);
                this.distributedCacheManager.remove(value, item, true);
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

    @Override
    public List<RegisteredService> updateLoadedRegisteredServicesFromCache(final List<RegisteredService> services,
                                                                           final ServiceRegistry serviceRegistry) {
        val cachedServices = distributedCacheManager.getAll();

        for (val entry : cachedServices) {
            val cachedService = entry.getValue();
            LOGGER.debug("Found cached service definition [{}] in the replication cache [{}]",
                cachedService, distributedCacheManager.getName());

            if (isRegisteredServiceMarkedAsDeletedInCache(entry)) {
                LOGGER.debug("Service found in the cache [{}] is marked as a deleted service. CAS will update the service registry "
                    + "of this CAS node to remove the local service, if found.", cachedService);
                serviceRegistry.delete(cachedService);
                distributedCacheManager.remove(cachedService, entry, true);
                continue;
            }

            val matchingService = services.stream()
                .filter(s -> s.getId() == cachedService.getId())
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

    private static boolean isRegisteredServiceMarkedAsDeletedInCache(final DistributedCacheObject<RegisteredService> item) {
        if (item.containsProperty("event")) {
            val event = item.getProperty("event", String.class);
            return event.equalsIgnoreCase(CasRegisteredServiceDeletedEvent.class.getSimpleName());
        }
        return false;
    }

    private void saveRegisteredServiceIfNecessary(final ServiceRegistry serviceRegistry, final RegisteredService value) {
        if (properties.getReplicationMode() == StreamingServiceRegistryProperties.ReplicationModes.ACTIVE_ACTIVE) {
            serviceRegistry.save(value);
        }
    }

    private void updateServiceRegistryWithNoMatchingService(final List<RegisteredService> services, final RegisteredService cachedService,
                                                            final ServiceRegistry serviceRegistry) {
        LOGGER.debug("No corresponding service definition could be matched against cache entry [{}] locally. "
            + "CAS will update the service registry of this CAS node with the cache entry for future look-ups", cachedService);
        updateServiceRegistryWithRegisteredService(services, cachedService, serviceRegistry);
    }

    private void updateServiceRegistryWithMatchingService(final List<RegisteredService> services, final RegisteredService cachedService,
                                                          final RegisteredService matchingService, final ServiceRegistry serviceRegistry) {
        LOGGER.debug("Found corresponding service definition [{}] locally via cache manager [{}]", matchingService, distributedCacheManager.getName());
        if (matchingService.equals(cachedService)) {
            LOGGER.debug("Service definition cache entry [{}] is the same as service definition found locally [{}]", cachedService, matchingService);
        } else {
            LOGGER.debug("Service definition found in the cache [{}] is more recent than its counterpart on this CAS node. "
                + "CAS will update the service registry of this CAS node with the cache entry for future look-ups", cachedService);
            updateServiceRegistryWithRegisteredService(services, cachedService, serviceRegistry);
        }
    }

    private void updateServiceRegistryWithRegisteredService(final List<RegisteredService> services,
                                                            final RegisteredService cachedService,
                                                            final ServiceRegistry serviceRegistry) {
        saveRegisteredServiceIfNecessary(serviceRegistry, cachedService);
        services.add(cachedService);
    }
}
