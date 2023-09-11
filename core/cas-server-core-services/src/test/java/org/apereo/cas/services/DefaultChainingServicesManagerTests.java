package org.apereo.cas.services;

import org.apereo.cas.services.mgmt.DefaultChainingServicesManager;
import org.apereo.cas.util.RandomUtils;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultChainingServicesManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RegisteredService")
class DefaultChainingServicesManagerTests extends AbstractServicesManagerTests<DefaultChainingServicesManager> {
    @Test
    void verifyOperation() throws Throwable {
        val mock = mock(ServicesManager.class);
        when(mock.findServiceBy(anyLong(), any())).thenCallRealMethod();
        when(mock.findServiceByName(anyString(), any())).thenCallRealMethod();
        when(mock.count()).thenCallRealMethod();
        when(mock.getName()).thenCallRealMethod();
        when(mock.getOrder()).thenCallRealMethod();
        assertEquals(Ordered.LOWEST_PRECEDENCE, mock.getOrder());
        assertEquals(0, mock.count());
        assertNotNull(mock.getName());
        assertNull(mock.findServiceBy(0, CasRegisteredService.class));
    }

    @Test
    void verifySupports() throws Throwable {
        val r = new CasRegisteredService();
        r.setId(10);
        r.setName("domainService1");
        r.setServiceId("https://www.example.com/one");
        servicesManager.save(r);
        assertTrue(servicesManager.supports(CasRegisteredService.class));
        assertTrue(servicesManager.supports(r));
        assertTrue(servicesManager.supports(RegisteredServiceTestUtils.getService()));
    }

    @Test
    void verifySaveWithDomains() throws Throwable {
        val svc = new CasRegisteredService();
        svc.setId(RandomUtils.nextLong());
        svc.setName("domainService2");
        svc.setServiceId("https://www.example.com/" + svc.getId());
        assertNotNull(servicesManager.save(svc, false));
        assertEquals(1, servicesManager.getDomains().count());
        assertFalse(servicesManager.getServicesForDomain("example.org").isEmpty());
    }

    @Test
    void verifySaveInBulk() throws Throwable {
        servicesManager.deleteAll();
        servicesManager.save(() -> {
            val svc = new CasRegisteredService();
            svc.setId(RandomUtils.nextLong());
            svc.setName("domainService2");
            svc.setServiceId("https://www.example.com/" + svc.getId());
            return svc;
        }, Assertions::assertNotNull, 10);
        val results = servicesManager.load();
        assertEquals(10, results.size());
    }

    @Test
    void verifySaveInStreams() throws Throwable {
        servicesManager.deleteAll();
        val s1 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), true);
        val s2 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), true);
        servicesManager.save(Stream.of(s1, s2));
        val results = servicesManager.load();
        assertEquals(2, results.size());
    }

    @Override
    protected ServicesManager getServicesManagerInstance() {
        val chain = new DefaultChainingServicesManager();
        chain.registerServiceManager(super.getServicesManagerInstance());
        return chain;
    }
}
