package org.apereo.cas.services;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultChainingServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
class DefaultChainingServiceRegistryTests {

    private static RegisteredService newService() {
        val service = mock(RegisteredService.class);
        when(service.getId()).thenReturn(1234L);
        when(service.getName()).thenReturn("Test");
        when(service.getDescription()).thenReturn("Test");
        return service;
    }

    @Test
    void verifySync() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val chain = new DefaultChainingServiceRegistry(appCtx);
        val registry = new InMemoryServiceRegistry(appCtx);
        chain.addServiceRegistry(registry);
        val service = newService();
        registry.save(service);
        chain.synchronize(service);
        assertEquals(1, registry.size());
    }

    @Test
    void verifyChainOps() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val chain = new DefaultChainingServiceRegistry(appCtx);
        val registry1 = new InMemoryServiceRegistry(appCtx);
        chain.addServiceRegistry(registry1);

        val registry2 = new InMemoryServiceRegistry(appCtx);
        chain.addServiceRegistry(registry2);

        val service = newService();
        chain.save(service);

        assertNotNull(registry1.findServiceById(service.getId()));
        assertNotNull(registry2.findServiceById(service.getId()));

        chain.delete(service);

        assertNull(registry1.findServiceById(service.getId()));
        assertNull(registry2.findServiceById(service.getId()));
    }

    @Test
    void verifyFindServiceByHonorsEvaluationOrderAcrossRegistries() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val chain = new DefaultChainingServiceRegistry(appCtx);
        val registry1 = new InMemoryServiceRegistry(appCtx);
        registry1.setOrder(1);
        val registry2 = new InMemoryServiceRegistry(appCtx);
        registry2.setOrder(2);
        chain.addServiceRegistries(List.<ServiceRegistry>of(registry1, registry2));

        val broad = new CasRegisteredService();
        broad.setId(1000);
        broad.setName("Broad");
        broad.setServiceId("^https://app.example.org.*");
        broad.setEvaluationOrder(100);
        registry1.save(broad);

        val specific = new CasRegisteredService();
        specific.setId(2000);
        specific.setName("Specific");
        specific.setServiceId("^https://app.example.org/admin.*");
        specific.setEvaluationOrder(1);
        registry2.save(specific);

        assertEquals("Specific", chain.findServiceBy("https://app.example.org/admin/page").getName());
        assertEquals("Broad", chain.findServiceBy("https://app.example.org/home").getName());
        assertEquals(2, chain.getServicesStream().count());
    }
}
