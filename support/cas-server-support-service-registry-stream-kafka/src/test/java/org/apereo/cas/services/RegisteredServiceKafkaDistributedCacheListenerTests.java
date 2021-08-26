package org.apereo.cas.services;

import org.apereo.cas.config.CasServicesStreamingConfiguration;
import org.apereo.cas.config.CasServicesStreamingKafkaConfiguration;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheObject;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceKafkaDistributedCacheListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Kafka")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasServicesStreamingKafkaConfiguration.class,
    CasServicesStreamingConfiguration.class
}, properties = {
    "cas.service-registry.stream.kafka.bootstrap-address=localhost:9092",
    "cas.service-registry.stream.enabled=true"
})
@EnabledIfPortOpen(port = 9092)
public class RegisteredServiceKafkaDistributedCacheListenerTests {
    @Autowired
    @Qualifier("registeredServiceKafkaDistributedCacheListener")
    private RegisteredServiceKafkaDistributedCacheListener listener;

    @BeforeEach
    public void tearDown() {
        this.listener.getCacheManager().clear();
    }

    @Test
    public void verifyRemoval() {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val item = new DistributedCacheObject<RegisteredService>(
            Map.of("event", CasRegisteredServiceDeletedEvent.class.getSimpleName()),
            System.currentTimeMillis(),
            service, new PublisherIdentifier());

        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                listener.registeredServiceDistributedCacheKafkaListener(item);
            }
        });
    }

    @Test
    public void verifyUpdate() {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val item = new DistributedCacheObject<RegisteredService>(
            Map.of(),
            System.currentTimeMillis(),
            service, new PublisherIdentifier());

        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                listener.registeredServiceDistributedCacheKafkaListener(item);
            }
        });
    }

}
