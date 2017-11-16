package org.apereo.cas.services;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apereo.cas.DistributedCacheManager;
import org.apereo.cas.services.publisher.RegisteredServicesQueuedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link HazelcastRegisteredServiceDistributedCacheManager}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class HazelcastRegisteredServiceDistributedCacheManager extends
        BaseDistributedCacheManager<RegisteredService, RegisteredService, RegisteredServicesQueuedEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastRegisteredServiceDistributedCacheManager.class);

    private final HazelcastInstance instance;
    private final ServicesManager servicesManager;
    private final IMap<String, RegisteredServicesQueuedEvent> mapInstance;

    public HazelcastRegisteredServiceDistributedCacheManager(final HazelcastInstance instance,
                                                             final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
        this.instance = instance;
        
        final String mapName = instance.getConfig().getMapConfigs().keySet().iterator().next();
        LOGGER.debug("Retrieving Hazelcast map [{}] for service replication", mapName);
        this.mapInstance = instance.getMap(mapName);
    }

    @Override
    public void close() {
        this.instance.shutdown();
    }

    @Override
    public RegisteredService get(final RegisteredService service) {
        if (contains(service)) {
            final String key = RegisteredServicesQueuedEvent.buildKey(service);
            final RegisteredServicesQueuedEvent event = this.mapInstance.get(key);
            return event.getService();
        }
        return null;
    }

    @Override
    public void set(final RegisteredService key, final RegisteredServicesQueuedEvent event) {
        LOGGER.debug("Broadcasting event [{}] via Hazelcast...", event);
        this.mapInstance.set(event.getKey(), event);
    }

    @Override
    public boolean contains(final RegisteredService service) {
        final String key = RegisteredServicesQueuedEvent.buildKey(service);
        return this.mapInstance.containsKey(key);
    }

    @Override
    public void remove(final RegisteredService service) {
        final String key = RegisteredServicesQueuedEvent.buildKey(service);
        this.mapInstance.remove(key);
    }
}
