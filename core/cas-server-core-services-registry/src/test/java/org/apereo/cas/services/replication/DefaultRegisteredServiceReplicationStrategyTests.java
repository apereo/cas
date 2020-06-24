package org.apereo.cas.services.replication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.support.StaticApplicationContext;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultRegisteredServiceReplicationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class, properties = "cas.service-registry.stream.replication-mode=ACTIVE_ACTIVE")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("RegisteredService")
public class DefaultRegisteredServiceReplicationStrategyTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifySetInCache() {
        val id = new PublisherIdentifier();
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val serviceRegistry = new InMemoryServiceRegistry(appCtx);
        val stream = casProperties.getServiceRegistry().getStream();
        val mgr = mock(DistributedCacheManager.class);
        val strategy = new DefaultRegisteredServiceReplicationStrategy(mgr, stream, id);
        when(mgr.find(any())).thenReturn(Optional.empty());
        var svc = strategy.getRegisteredServiceFromCacheIfAny(newService("Test"), 1000, serviceRegistry);
        assertNotNull(svc);
        svc = strategy.getRegisteredServiceFromCacheIfAny(newService("Test"), "https://example.org", serviceRegistry);
        assertNotNull(svc);
        strategy.destroy();
    }

    @Test
    public void verifyGetInCacheAndRemove() {
        val id = new PublisherIdentifier();
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val serviceRegistry = new InMemoryServiceRegistry(appCtx);
        val stream = casProperties.getServiceRegistry().getStream();
        val mgr = mock(DistributedCacheManager.class);
        val strategy = new DefaultRegisteredServiceReplicationStrategy(mgr, stream, id);

        val service = newService("Test");

        val object = DistributedCacheObject.<RegisteredService>builder()
            .value(service)
            .publisherIdentifier(id)
            .properties(Map.of("event", CasRegisteredServiceDeletedEvent.class.getSimpleName()))
            .build();
        when(mgr.find(any())).thenReturn(Optional.of(object));

        val svc = strategy.getRegisteredServiceFromCacheIfAny(service, 1000, serviceRegistry);
        assertNotNull(svc);
        assertTrue(serviceRegistry.size() == 0);
    }

    @Test
    public void verifyGetInCacheAndSave() {
        val id = new PublisherIdentifier();
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val serviceRegistry = new InMemoryServiceRegistry(appCtx);
        val stream = casProperties.getServiceRegistry().getStream();
        val mgr = mock(DistributedCacheManager.class);
        val strategy = new DefaultRegisteredServiceReplicationStrategy(mgr, stream, id);

        val service = newService("Test");
        val object = DistributedCacheObject.<RegisteredService>builder()
            .value(service)
            .publisherIdentifier(id)
            .build();
        when(mgr.find(any())).thenReturn(Optional.of(object));

        val svc = strategy.getRegisteredServiceFromCacheIfAny(null, 1000, serviceRegistry);
        assertNotNull(svc);
        assertTrue(serviceRegistry.size() == 1);
    }

    @Test
    public void verifyGetInCacheAndUpdate() {
        val id = new PublisherIdentifier();
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val serviceRegistry = new InMemoryServiceRegistry(appCtx);
        val stream = casProperties.getServiceRegistry().getStream();
        val mgr = mock(DistributedCacheManager.class);
        val strategy = new DefaultRegisteredServiceReplicationStrategy(mgr, stream, id);

        val service = newService("Test");
        val service2 = newService("Test1");
        val object = DistributedCacheObject.<RegisteredService>builder()
            .value(service2)
            .publisherIdentifier(id)
            .build();
        when(mgr.find(any())).thenReturn(Optional.of(object));

        var svc = strategy.getRegisteredServiceFromCacheIfAny(service, 1000, serviceRegistry);
        assertNotNull(svc);
        assertTrue(serviceRegistry.size() == 1);
        svc = serviceRegistry.findServiceById(1000);
        assertEquals("Test1", svc.getName());
    }

    @Test
    public void verifyGetInCacheAndMatch() {
        val id = new PublisherIdentifier();
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val serviceRegistry = new InMemoryServiceRegistry(appCtx);
        val stream = casProperties.getServiceRegistry().getStream();
        val mgr = mock(DistributedCacheManager.class);
        val strategy = new DefaultRegisteredServiceReplicationStrategy(mgr, stream, id);

        val service = newService("Test");
        val service2 = newService("Test");
        val object = DistributedCacheObject.<RegisteredService>builder()
            .value(service2)
            .publisherIdentifier(id)
            .build();
        when(mgr.find(any())).thenReturn(Optional.of(object));

        var svc = strategy.getRegisteredServiceFromCacheIfAny(service, 1000, serviceRegistry);
        assertNotNull(svc);
        assertTrue(serviceRegistry.size() == 0);
    }

    @Test
    public void verifyUpdateWithMatch() {
        val id = new PublisherIdentifier();
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val serviceRegistry = new InMemoryServiceRegistry(appCtx);
        val stream = casProperties.getServiceRegistry().getStream();
        val mgr = mock(DistributedCacheManager.class);
        val service = newService("Test1");
        val service2 = newService("Test2");
        val obj1 = DistributedCacheObject.<RegisteredService>builder()
            .value(service)
            .publisherIdentifier(id)
            .build();
        val object = DistributedCacheObject.<RegisteredService>builder()
            .value(service)
            .publisherIdentifier(id)
            .properties(Map.of("event", CasRegisteredServiceDeletedEvent.class.getSimpleName()))
            .build();
        when(mgr.getAll()).thenReturn(CollectionUtils.wrapList(obj1, object));
        val strategy = new DefaultRegisteredServiceReplicationStrategy(mgr, stream, id);

        val results = strategy.updateLoadedRegisteredServicesFromCache(CollectionUtils.wrapList(service, service2), serviceRegistry);
        assertFalse(results.isEmpty());
    }

    @Test
    public void verifyUpdateWithNoMatch() {
        val id = new PublisherIdentifier();
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val serviceRegistry = new InMemoryServiceRegistry(appCtx);
        val stream = casProperties.getServiceRegistry().getStream();
        val mgr = mock(DistributedCacheManager.class);

        val service = newService("Test1");
        service.setId(500);

        val cachedService = newService("Test2");
        val object = DistributedCacheObject.<RegisteredService>builder()
            .value(cachedService)
            .publisherIdentifier(id)
            .build();
        when(mgr.getAll()).thenReturn(CollectionUtils.wrapList(object));

        val strategy = new DefaultRegisteredServiceReplicationStrategy(mgr, stream, id);

        val results = strategy.updateLoadedRegisteredServicesFromCache(CollectionUtils.wrapList(service), serviceRegistry);
        assertFalse(results.isEmpty());
        assertEquals(2, results.size());
    }

    private static RegisteredService newService(final String name) {
        val service = new RegexRegisteredService();
        service.setServiceId("^https?://.*");
        service.setName(name);
        service.setId(1000L);
        service.setDescription("Test description");
        return service;
    }
}
