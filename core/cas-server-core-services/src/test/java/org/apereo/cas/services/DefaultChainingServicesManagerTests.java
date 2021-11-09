package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
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
public class DefaultChainingServicesManagerTests extends AbstractServicesManagerTests<DefaultChainingServicesManager> {
    @Test
    public void verifyOperation() {
        val servicesManager = mock(ServicesManager.class);
        when(servicesManager.findServiceBy(anyLong(), any())).thenCallRealMethod();
        when(servicesManager.findServiceByName(anyString(), any())).thenCallRealMethod();
        when(servicesManager.count()).thenCallRealMethod();
        when(servicesManager.getName()).thenCallRealMethod();
        when(servicesManager.getOrder()).thenCallRealMethod();
        assertEquals(Ordered.LOWEST_PRECEDENCE, servicesManager.getOrder());
        assertEquals(0, servicesManager.count());
        assertNotNull(servicesManager.getName());

        assertNull(servicesManager.findServiceBy(0, RegexRegisteredService.class));
        assertNull(servicesManager.findServiceBy(new WebApplicationServiceFactory().createService("name"), RegexRegisteredService.class));
    }

    @Test
    public void verifySupports() {
        val r = new RegexRegisteredService();
        r.setId(10);
        r.setName("domainService1");
        r.setServiceId("https://www.example.com/one");
        servicesManager.save(r);
        assertTrue(servicesManager.supports(RegexRegisteredService.class));
        assertTrue(servicesManager.supports(r));
        assertTrue(servicesManager.supports(RegisteredServiceTestUtils.getService()));
    }

    @Test
    public void verifySaveWithDomains() {
        val svc = new RegexRegisteredService();
        svc.setId(RandomUtils.nextLong());
        svc.setName("domainService2");
        svc.setServiceId("https://www.example.com/" + svc.getId());
        assertNotNull(servicesManager.save(svc, false));
        assertEquals(servicesManager.getDomains().count(), 1);
        assertFalse(servicesManager.getServicesForDomain("example.org").isEmpty());
    }

    @Test
    public void verifySaveInBulk() {
        servicesManager.deleteAll();
        servicesManager.save(() -> {
            val svc = new RegexRegisteredService();
            svc.setId(RandomUtils.nextLong());
            svc.setName("domainService2");
            svc.setServiceId("https://www.example.com/" + svc.getId());
            return svc;
        }, Assertions::assertNotNull, 10);
        val results = servicesManager.load();
        assertEquals(10, results.size());
    }

    @Test
    public void verifySaveInStreams() {
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
