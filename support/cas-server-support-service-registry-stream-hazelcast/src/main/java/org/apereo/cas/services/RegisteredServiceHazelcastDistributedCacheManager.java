package org.apereo.cas.services;

import org.apereo.cas.util.cache.DistributedCacheObject;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This is {@link RegisteredServiceHazelcastDistributedCacheManager}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class RegisteredServiceHazelcastDistributedCacheManager extends
    BaseDistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>> {

    private final HazelcastInstance instance;
    private final IMap<String, DistributedCacheObject<RegisteredService>> mapInstance;

    public RegisteredServiceHazelcastDistributedCacheManager(final HazelcastInstance instance) {
        this.instance = instance;

        val mapName = instance.getConfig().getMapConfigs().keySet().iterator().next();
        LOGGER.debug("Retrieving Hazelcast map [{}] for service replication", mapName);
        this.mapInstance = instance.getMap(mapName);
    }

    /**
     * Gets key.
     *
     * @param service the service
     * @return the key
     */
    public static String buildKey(final RegisteredService service) {
        return service.getId() + ";" + service.getName() + ';' + service.getServiceId();
    }

    @Override
    public void close() {
        this.instance.shutdown();
    }

    @Override
    public Collection<DistributedCacheObject<RegisteredService>> getAll() {
        return this.mapInstance.values();
    }

    @Override
    public DistributedCacheObject<RegisteredService> get(final RegisteredService service) {
        if (contains(service)) {
            val key = buildKey(service);
            return this.mapInstance.get(key);
        }
        return null;
    }

    @Override
    public void set(final RegisteredService key, final DistributedCacheObject<RegisteredService> item) {
        LOGGER.debug("Broadcasting service definition [{}] via Hazelcast...", item);
        this.mapInstance.set(buildKey(key), item);
    }

    @Override
    public boolean contains(final RegisteredService service) {
        val key = buildKey(service);
        return this.mapInstance.containsKey(key);
    }

    @Override
    public void remove(final RegisteredService service, final DistributedCacheObject<RegisteredService> item) {
        val key = buildKey(service);
        this.mapInstance.remove(key);
    }

    @Override
    public void update(final RegisteredService service, final DistributedCacheObject<RegisteredService> item) {
        remove(service, item);
        set(service, item);
    }

    @Override
    public Collection<DistributedCacheObject<RegisteredService>> findAll(
        final Predicate<DistributedCacheObject<RegisteredService>> filter) {
        return getAll().stream().filter(filter).collect(Collectors.toList());
    }
}
