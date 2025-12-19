package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.config.CasServicesStreamingAutoConfiguration;
import org.apereo.cas.config.CasServicesStreamingKafkaAutoConfiguration;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheObject;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceKafkaDistributedCacheListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Kafka")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasServicesStreamingKafkaAutoConfiguration.class,
    CasServicesStreamingAutoConfiguration.class
}, properties = {
    "cas.service-registry.stream.kafka.bootstrap-address=localhost:9092",
    "cas.service-registry.stream.core.enabled=true"
})
@EnabledIfListeningOnPort(port = 9092)
class RegisteredServiceKafkaDistributedCacheListenerTests {
    @Autowired
    @Qualifier("registeredServiceKafkaDistributedCacheListener")
    private RegisteredServiceKafkaDistributedCacheListener listener;

    @BeforeEach
    void tearDown() {
        listener.getCacheManager().clear();
    }

    @Test
    void verifyRemoval() {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val item = new DistributedCacheObject<RegisteredService>(
            Map.of("event", CasRegisteredServiceDeletedEvent.class.getSimpleName()),
            System.currentTimeMillis(),
            service, new PublisherIdentifier());

        assertDoesNotThrow(() -> listener.registeredServiceDistributedCacheKafkaListener(item));
    }

    @Test
    void verifyUpdate() {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val item = new DistributedCacheObject<RegisteredService>(
            Map.of(),
            System.currentTimeMillis(),
            service, new PublisherIdentifier());

        assertDoesNotThrow(() -> listener.registeredServiceDistributedCacheKafkaListener(item));
    }

}
