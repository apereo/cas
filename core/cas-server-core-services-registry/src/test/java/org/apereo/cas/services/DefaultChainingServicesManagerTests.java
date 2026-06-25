package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.mgmt.DefaultChainingServicesManager;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultChainingServicesManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("RegisteredService")
class DefaultChainingServicesManagerTests {

    private static RegisteredService newService() {
        val service = mock(RegisteredService.class);
        when(service.getId()).thenReturn(RandomUtils.nextLong());
        when(service.getName()).thenReturn("Test");
        when(service.getDescription()).thenReturn("Test");
        return service;
    }

    private static ServicesManager newServicesManager() {
        val servicesManager = mock(ServicesManager.class);
        when(servicesManager.supports(any(Service.class))).thenReturn(true);
        when(servicesManager.supports(any(RegisteredService.class))).thenReturn(true);
        return servicesManager;
    }

    private static CacheableServicesManager newCacheableServicesManager() {
        val manager = mock(CacheableServicesManager.class);
        when(manager.supports(any(Service.class))).thenReturn(true);
        when(manager.supports(any(RegisteredService.class))).thenReturn(true);
        when(manager.supports(any(Class.class))).thenReturn(true);
        return manager;
    }

    @Test
    void verifyChainServiceManager() {
        try (val appCtx = new StaticApplicationContext()) {
            appCtx.refresh();
            val servicesManager = new DefaultChainingServicesManager();
            servicesManager.registerServiceManager(newServicesManager());
            servicesManager.registerServiceManager(newServicesManager());
            servicesManager.save(Stream.of(newService(), newService(), newService()));
        }
    }

    @Test
    void verifyGetCachedRegisteredServices() {
        val chaining = new DefaultChainingServicesManager();

        val service1 = newService();
        val service2 = newService();
        val id1 = service1.getId();
        val id2 = service2.getId();
        val mgr1 = newCacheableServicesManager();
        when(mgr1.getCachedRegisteredServices()).thenReturn(Map.of(id1, service1));
        val mgr2 = newCacheableServicesManager();
        when(mgr2.getCachedRegisteredServices()).thenReturn(Map.of(id2, service2));

        chaining.registerServiceManager(mgr1);
        chaining.registerServiceManager(mgr2);

        val result = chaining.getCachedRegisteredServices();
        assertEquals(2, result.size());
        assertTrue(result.containsKey(service1.getId()));
        assertTrue(result.containsKey(service2.getId()));
    }

    @Test
    void verifyGetCachedRegisteredServicesSize() {
        val chaining = new DefaultChainingServicesManager();

        val mgr1 = newCacheableServicesManager();
        when(mgr1.getCachedRegisteredServicesSize()).thenReturn(3L);
        val mgr2 = newCacheableServicesManager();
        when(mgr2.getCachedRegisteredServicesSize()).thenReturn(5L);

        chaining.registerServiceManager(mgr1);
        chaining.registerServiceManager(mgr2);

        assertEquals(8L, chaining.getCachedRegisteredServicesSize());
    }

    @Test
    void verifyCleanRegisteredServicesCache() {
        val chaining = new DefaultChainingServicesManager();
        val mgr1 = newCacheableServicesManager();
        val mgr2 = newCacheableServicesManager();
        chaining.registerServiceManager(mgr1);
        chaining.registerServiceManager(mgr2);

        chaining.cleanRegisteredServicesCache();

        verify(mgr1, times(1)).cleanRegisteredServicesCache();
        verify(mgr2, times(1)).cleanRegisteredServicesCache();
    }

    @Test
    void verifyCacheRegisteredService() {
        val chaining = new DefaultChainingServicesManager();
        val mgr = newCacheableServicesManager();
        chaining.registerServiceManager(mgr);

        val service = newService();
        chaining.cacheRegisteredService(service);

        verify(mgr, times(1)).cacheRegisteredService(service);
    }

    @Test
    void verifyCacheRegisteredServiceSkipsNonCacheableManagers() {
        val chaining = new DefaultChainingServicesManager();
        val plain = newServicesManager();
        val cacheable = newCacheableServicesManager();
        chaining.registerServiceManager(plain);
        chaining.registerServiceManager(cacheable);

        val service = newService();
        chaining.cacheRegisteredService(service);

        verify(cacheable, times(1)).cacheRegisteredService(service);
        verify(plain, never()).save(any(RegisteredService.class));
    }

    @Test
    void verifyCacheRegisteredServices() {
        val chaining = new DefaultChainingServicesManager();
        val service = newService();
        val input = Map.of(service.getId(), service);

        val mgr1 = newCacheableServicesManager();
        when(mgr1.cacheRegisteredServices(input)).thenReturn(input);
        val mgr2 = newCacheableServicesManager();
        when(mgr2.cacheRegisteredServices(input)).thenReturn(Map.of());

        chaining.registerServiceManager(mgr1);
        chaining.registerServiceManager(mgr2);

        val result = chaining.cacheRegisteredServices(input);
        assertFalse(result.isEmpty());
        assertTrue(result.containsKey(service.getId()));
        verify(mgr1, times(1)).cacheRegisteredServices(input);
        verify(mgr2, times(1)).cacheRegisteredServices(input);
    }

    @Test
    void verifyRemoveRegisteredServiceFromCache() {
        val chaining = new DefaultChainingServicesManager();
        val mgr = newCacheableServicesManager();
        chaining.registerServiceManager(mgr);

        val service = newService();
        chaining.removeRegisteredServiceFromCache(service);

        verify(mgr, times(1)).removeRegisteredServiceFromCache(service);
    }

    @Test
    void verifyRemoveRegisteredServicesFromCache() {
        val chaining = new DefaultChainingServicesManager();
        val mgr1 = newCacheableServicesManager();
        val mgr2 = newCacheableServicesManager();
        chaining.registerServiceManager(mgr1);
        chaining.registerServiceManager(mgr2);

        chaining.removeRegisteredServicesFromCache();

        verify(mgr1, times(1)).removeRegisteredServicesFromCache();
        verify(mgr2, times(1)).removeRegisteredServicesFromCache();
    }

    @Test
    void verifyFindCachedRegisteredService() {
        val chaining = new DefaultChainingServicesManager();
        val service = newService();

        val mgr1 = newCacheableServicesManager();
        when(mgr1.findCachedRegisteredService(service.getId(), RegisteredService.class)).thenReturn(null);
        val mgr2 = newCacheableServicesManager();
        when(mgr2.findCachedRegisteredService(service.getId(), RegisteredService.class)).thenReturn(service);

        chaining.registerServiceManager(mgr1);
        chaining.registerServiceManager(mgr2);

        val found = chaining.findCachedRegisteredService(service.getId(), RegisteredService.class);
        assertNotNull(found);
        assertEquals(service.getId(), found.getId());
    }

    @Test
    void verifyFindCachedRegisteredServiceReturnsNullWhenNotFound() {
        val chaining = new DefaultChainingServicesManager();
        val mgr = newCacheableServicesManager();
        when(mgr.findCachedRegisteredService(anyLong(), any())).thenReturn(null);
        chaining.registerServiceManager(mgr);

        assertNull(chaining.findCachedRegisteredService(999L, RegisteredService.class));
    }

    @Test
    void verifyNoCacheableManagersReturnDefaults() {
        val chaining = new DefaultChainingServicesManager();
        chaining.registerServiceManager(newServicesManager());

        assertTrue(chaining.getCachedRegisteredServices().isEmpty());
        assertEquals(0L, chaining.getCachedRegisteredServicesSize());
        assertNull(chaining.findCachedRegisteredService(1L, RegisteredService.class));
    }
}
