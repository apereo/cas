package org.apereo.cas.config;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.publisher.CasRegisteredServiceStreamPublisher;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.StaticApplicationContext;
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
class CasServicesStreamingKafkaConfigurationTests {
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
    void verifyOperation() {
        assertNotNull(registeredServiceDistributedCacheManager);
        assertNotNull(casRegisteredServiceStreamPublisher);
    }

    @Test
    void verifySerialization() throws Throwable {
        val o = DistributedCacheObject.<RegisteredService>builder()
            .value(RegisteredServiceTestUtils.getRegisteredService())
            .publisherIdentifier(new PublisherIdentifier())
            .build();
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val file = new File(FileUtils.getTempDirectoryPath(), UUID.randomUUID() + ".json");
        val mapper = new RegisteredServiceJsonSerializer(appCtx).getObjectMapper();
        mapper.writeValue(file, o);
        val readPolicy = mapper.readValue(file, DistributedCacheObject.class);
        assertEquals(o, readPolicy);
    }

    @Test
    void verifyListener() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val publisherId = new PublisherIdentifier();
        val clientInfo = ClientInfoHolder.getClientInfo();
        casRegisteredServiceStreamPublisher.publish(registeredService,
            new CasRegisteredServiceSavedEvent(this, registeredService, clientInfo), publisherId);
        Thread.sleep(3000);
        assertFalse(registeredServiceDistributedCacheManager.getAll().isEmpty());

        casRegisteredServiceStreamPublisher.publish(registeredService,
            new CasRegisteredServiceDeletedEvent(this, registeredService, clientInfo), publisherId);

        Thread.sleep(2500);
        registeredServiceDistributedCacheManager.clear();
        assertTrue(registeredServiceDistributedCacheManager.getAll().isEmpty());
    }

    @Test
    void verifyAction() throws Throwable {
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
    void verifyPublisher() {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val clientInfo = ClientInfoHolder.getClientInfo();
        casRegisteredServiceStreamPublisher.publish(registeredService,
            new CasRegisteredServiceDeletedEvent(this, registeredService, clientInfo),
            casRegisteredServiceStreamPublisherIdentifier);
        casRegisteredServiceStreamPublisher.publish(registeredService,
            new CasRegisteredServiceSavedEvent(this, registeredService, clientInfo),
            casRegisteredServiceStreamPublisherIdentifier);
        casRegisteredServiceStreamPublisher.publish(registeredService,
            new CasRegisteredServiceLoadedEvent(this, registeredService, clientInfo),
            casRegisteredServiceStreamPublisherIdentifier);
        assertFalse(registeredServiceDistributedCacheManager.getAll().isEmpty());
    }
}
