package org.apereo.cas.services.replication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.util.CollectionUtils;
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
@Tag("Simple")
public class DefaultRegisteredServiceReplicationStrategyTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifySetInCache() throws Exception {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val serviceRegistry = new InMemoryServiceRegistry(appCtx);
        val stream = casProperties.getServiceRegistry().getStream();
        val mgr = mock(DistributedCacheManager.class);
        val strategy = new DefaultRegisteredServiceReplicationStrategy(mgr, stream);
        when(mgr.find(any())).thenReturn(Optional.empty());
        var svc = strategy.getRegisteredServiceFromCacheIfAny(newService("Test"), 1000, serviceRegistry);
        assertNotNull(svc);
        svc = strategy.getRegisteredServiceFromCacheIfAny(newService("Test"), "https://example.org", serviceRegistry);
        assertNotNull(svc);
        strategy.destroy();
    }

    @Test
    public void verifyGetInCacheAndRemove() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val serviceRegistry = new InMemoryServiceRegistry(appCtx);
        val stream = casProperties.getServiceRegistry().getStream();
        val mgr = mock(DistributedCacheManager.class);
        val strategy = new DefaultRegisteredServiceReplicationStrategy(mgr, stream);

        val service = newService("Test");
        val object = new DistributedCacheObject<>(service);
        object.getProperties().put("event", new CasRegisteredServiceDeletedEvent(this, service));
        when(mgr.find(any())).thenReturn(Optional.of(object));

        val svc = strategy.getRegisteredServiceFromCacheIfAny(service, 1000, serviceRegistry);
        assertNotNull(svc);
        assertTrue(serviceRegistry.size() == 0);
    }

    @Test
    public void verifyGetInCacheAndSave() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val serviceRegistry = new InMemoryServiceRegistry(appCtx);
        val stream = casProperties.getServiceRegistry().getStream();
        val mgr = mock(DistributedCacheManager.class);
        val strategy = new DefaultRegisteredServiceReplicationStrategy(mgr, stream);

        val service = newService("Test");
        val object = new DistributedCacheObject<>(service);
        when(mgr.find(any())).thenReturn(Optional.of(object));

        val svc = strategy.getRegisteredServiceFromCacheIfAny(null, 1000, serviceRegistry);
        assertNotNull(svc);
        assertTrue(serviceRegistry.size() == 1);
    }

    @Test
    public void verifyGetInCacheAndUpdate() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val serviceRegistry = new InMemoryServiceRegistry(appCtx);
        val stream = casProperties.getServiceRegistry().getStream();
        val mgr = mock(DistributedCacheManager.class);
        val strategy = new DefaultRegisteredServiceReplicationStrategy(mgr, stream);

        val service = newService("Test");
        val service2 = newService("Test1");
        val object = new DistributedCacheObject<>(service2);
        when(mgr.find(any())).thenReturn(Optional.of(object));

        var svc = strategy.getRegisteredServiceFromCacheIfAny(service, 1000, serviceRegistry);
        assertNotNull(svc);
        assertTrue(serviceRegistry.size() == 1);
        svc = serviceRegistry.findServiceById(1000);
        assertEquals("Test1", svc.getName());
    }

    @Test
    public void verifyGetInCacheAndMatch() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val serviceRegistry = new InMemoryServiceRegistry(appCtx);
        val stream = casProperties.getServiceRegistry().getStream();
        val mgr = mock(DistributedCacheManager.class);
        val strategy = new DefaultRegisteredServiceReplicationStrategy(mgr, stream);

        val service = newService("Test");
        val service2 = newService("Test");
        val object = new DistributedCacheObject<>(service2);
        when(mgr.find(any())).thenReturn(Optional.of(object));

        var svc = strategy.getRegisteredServiceFromCacheIfAny(service, 1000, serviceRegistry);
        assertNotNull(svc);
        assertTrue(serviceRegistry.size() == 0);
    }

    @Test
    public void verifyUpdateWithMatch() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val serviceRegistry = new InMemoryServiceRegistry(appCtx);
        val stream = casProperties.getServiceRegistry().getStream();
        val mgr = mock(DistributedCacheManager.class);
        val service = newService("Test1");
        val service2 = newService("Test2");
        val obj1 = new DistributedCacheObject<>(service);
        obj1.getProperties().put("event", new CasRegisteredServiceDeletedEvent(this, service));

        when(mgr.getAll()).thenReturn(CollectionUtils.wrapList(obj1, new DistributedCacheObject<>(service2)));
        val strategy = new DefaultRegisteredServiceReplicationStrategy(mgr, stream);

        val results = strategy.updateLoadedRegisteredServicesFromCache(CollectionUtils.wrapList(service, service2), serviceRegistry);
        assertFalse(results.isEmpty());
    }

    @Test
    public void verifyUpdateWithNoMatch() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val serviceRegistry = new InMemoryServiceRegistry(appCtx);
        val stream = casProperties.getServiceRegistry().getStream();
        val mgr = mock(DistributedCacheManager.class);

        val service = newService("Test1");
        service.setId(500);

        val cachedService = newService("Test2");
        when(mgr.getAll()).thenReturn(CollectionUtils.wrapList(new DistributedCacheObject<>(cachedService)));

        val strategy = new DefaultRegisteredServiceReplicationStrategy(mgr, stream);

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
