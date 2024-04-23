package org.apereo.cas.services;

import org.apereo.cas.config.CasServicesStreamingAutoConfiguration;
import org.apereo.cas.config.CasServicesStreamingKafkaAutoConfiguration;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import java.util.Map;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceKafkaDistributedCacheManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Kafka")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasServicesStreamingKafkaAutoConfiguration.class,
    CasServicesStreamingAutoConfiguration.class
}, properties = {
    "cas.service-registry.stream.kafka.bootstrap-address=localhost:9092",
    "cas.service-registry.stream.core.enabled=true"
})
@EnabledIfListeningOnPort(port = 9092)
class RegisteredServiceKafkaDistributedCacheManagerTests {

    @Autowired
    @Qualifier("registeredServiceDistributedCacheManager")
    private DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>, PublisherIdentifier>
        registeredServiceDistributedCacheManager;

    @Test
    void verifyOperation() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        assertFalse(registeredServiceDistributedCacheManager.contains(service));
        assertTrue(registeredServiceDistributedCacheManager.getAll().isEmpty());
        assertTrue(registeredServiceDistributedCacheManager.findAll(Objects::nonNull).isEmpty());

        val item = new DistributedCacheObject<RegisteredService>(Map.of(),
            System.currentTimeMillis(),
            service, new PublisherIdentifier());
        assertNotNull(registeredServiceDistributedCacheManager.set(service, item, true));
        assertNotNull(registeredServiceDistributedCacheManager.set(service, item, false));

        assertNotNull(registeredServiceDistributedCacheManager.update(service, item, true));
        assertNotNull(registeredServiceDistributedCacheManager.update(service, item, false));
    }


    @BeforeEach
    public void tearDown() {
        registeredServiceDistributedCacheManager.clear();
    }


}
