package org.apereo.cas.config;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.publisher.CasRegisteredServiceStreamPublisher;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasServicesStreamingHazelcastConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Hazelcast")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasServicesStreamingHazelcastConfiguration.class,
    CasServicesStreamingConfiguration.class
}, properties = {
    "cas.service-registry.stream.hazelcast.config.cluster.core.instance-name=servicesRegistryStream",
    "cas.service-registry.stream.enabled=true"
})
public class CasServicesStreamingHazelcastConfigurationTests {
    @Autowired
    @Qualifier("registeredServiceDistributedCacheManager")
    private DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>, PublisherIdentifier> registeredServiceDistributedCacheManager;

    @Autowired
    @Qualifier("casRegisteredServiceStreamPublisher")
    private CasRegisteredServiceStreamPublisher casRegisteredServiceStreamPublisher;

    @Test
    public void verifyOperation() {
        assertNotNull(registeredServiceDistributedCacheManager);
        assertNotNull(casRegisteredServiceStreamPublisher);
    }

}
