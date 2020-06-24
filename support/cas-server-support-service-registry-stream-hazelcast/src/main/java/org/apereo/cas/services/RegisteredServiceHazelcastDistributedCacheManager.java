package org.apereo.cas.services;

import org.apereo.cas.util.cache.DistributedCacheObject;
import org.apereo.cas.util.cache.MappableDistributedCacheManager;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link RegisteredServiceHazelcastDistributedCacheManager}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class RegisteredServiceHazelcastDistributedCacheManager extends
    MappableDistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>> {

    private final HazelcastInstance instance;

    public RegisteredServiceHazelcastDistributedCacheManager(final HazelcastInstance instance,
                                                             final IMap<String, DistributedCacheObject<RegisteredService>> mapInstance) {
        super(mapInstance);
        this.instance = instance;
    }

    @Override
    public void close() {
        this.instance.shutdown();
    }

    @Override
    protected String buildKey(final RegisteredService service) {
        return service.getId() + ";" + service.getName() + ';' + service.getServiceId();
    }
}
