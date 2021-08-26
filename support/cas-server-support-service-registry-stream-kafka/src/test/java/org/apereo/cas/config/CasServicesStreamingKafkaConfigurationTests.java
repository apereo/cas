package org.apereo.cas.config;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.publisher.CasRegisteredServiceStreamPublisher;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasServicesStreamingKafkaConfigurationTests}.
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
public class CasServicesStreamingKafkaConfigurationTests {
    @Autowired
    @Qualifier("registeredServiceDistributedCacheManager")
    private DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>, PublisherIdentifier> registeredServiceDistributedCacheManager;

    @Autowired
    @Qualifier("casRegisteredServiceStreamPublisher")
    private CasRegisteredServiceStreamPublisher casRegisteredServiceStreamPublisher;

    @Autowired
    @Qualifier("casRegisteredServiceStreamPublisherIdentifier")
    private PublisherIdentifier casRegisteredServiceStreamPublisherIdentifier;

    @Test
    public void verifyOperation() {
        assertNotNull(registeredServiceDistributedCacheManager);
        assertNotNull(casRegisteredServiceStreamPublisher);
    }

    @Test
    public void verifySerialization() throws Exception {
        val o = DistributedCacheObject.<RegisteredService>builder()
            .value(RegisteredServiceTestUtils.getRegisteredService())
            .publisherIdentifier(new PublisherIdentifier())
            .build();
        val file = new File(FileUtils.getTempDirectoryPath(), UUID.randomUUID().toString() + ".json");
        val mapper = new RegisteredServiceJsonSerializer().getObjectMapper();
        mapper.writeValue(file, o);
        val readPolicy = mapper.readValue(file, DistributedCacheObject.class);
        assertEquals(o, readPolicy);
    }

    @Test
    public void verifyListener() throws Exception {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val publisherId = new PublisherIdentifier();
        
        casRegisteredServiceStreamPublisher.publish(registeredService,
            new CasRegisteredServiceSavedEvent(this, registeredService), publisherId);
        Thread.sleep(3000);
        assertFalse(registeredServiceDistributedCacheManager.getAll().isEmpty());

        casRegisteredServiceStreamPublisher.publish(registeredService,
            new CasRegisteredServiceDeletedEvent(this, registeredService), publisherId);

        Thread.sleep(2500);
        registeredServiceDistributedCacheManager.clear();
        assertTrue(registeredServiceDistributedCacheManager.getAll().isEmpty());
    }

    @Test
    public void verifyAction() throws Exception {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        var obj = registeredServiceDistributedCacheManager.get(registeredService);
        assertNull(obj);
        assertFalse(registeredServiceDistributedCacheManager.contains(registeredService));

        val cache = DistributedCacheObject.<RegisteredService>builder()
            .value(registeredService)
            .publisherIdentifier(casRegisteredServiceStreamPublisherIdentifier)
            .build();

        registeredServiceDistributedCacheManager.set(registeredService, cache, true);
        Thread.sleep(2000);

        assertFalse(registeredServiceDistributedCacheManager.getAll().isEmpty());

        obj = registeredServiceDistributedCacheManager.get(registeredService);
        assertNotNull(obj);

        var c = registeredServiceDistributedCacheManager.findAll(obj1 -> obj1.getValue().equals(registeredService));
        assertFalse(c.isEmpty());

        registeredServiceDistributedCacheManager.remove(registeredService, cache, true);
        Thread.sleep(5000);
        c = registeredServiceDistributedCacheManager.findAll(obj1 -> obj1.getValue().equals(registeredService));
        assertTrue(c.isEmpty());
        registeredServiceDistributedCacheManager.clear();
        assertTrue(registeredServiceDistributedCacheManager.getAll().isEmpty());
    }

    @Test
    public void verifyPublisher() {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        casRegisteredServiceStreamPublisher.publish(registeredService,
            new CasRegisteredServiceDeletedEvent(this, registeredService),
            casRegisteredServiceStreamPublisherIdentifier);
        casRegisteredServiceStreamPublisher.publish(registeredService,
            new CasRegisteredServiceSavedEvent(this, registeredService),
            casRegisteredServiceStreamPublisherIdentifier);
        casRegisteredServiceStreamPublisher.publish(registeredService,
            new CasRegisteredServiceLoadedEvent(this, registeredService),
            casRegisteredServiceStreamPublisherIdentifier);
        assertFalse(registeredServiceDistributedCacheManager.getAll().isEmpty());
    }
}
