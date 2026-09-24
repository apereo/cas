package org.apereo.cas.jmx.services;

import module java.base;
import org.apereo.cas.jmx.BaseCasJmxTests;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ServicesManagerManagedResourceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseCasJmxTests.SharedTestConfiguration.class)
@Tag("JMX")
@ExtendWith(CasTestExtension.class)
class ServicesManagerManagedResourceTests {

    @Autowired
    @Qualifier("servicesManagerManagedResource")
    private ServicesManagerManagedResource servicesManagerManagedResource;

    @Test
    void verifyOperation() {
        assertNotNull(this.servicesManagerManagedResource);
        assertNotNull(this.servicesManagerManagedResource.getServices());
    }

    @Test
    void verifyServiceInventoryAndStreamClosure() {
        val service = new CasRegisteredService();
        service.setId(42);
        service.setName("Sample");
        service.setServiceId("https://example.org/.*");
        val closed = new AtomicInteger();
        val manager = mock(ServicesManager.class);
        when(manager.stream()).thenAnswer(_ -> Stream.of(service).onClose(closed::incrementAndGet));
        val resource = new ServicesManagerManagedResource(manager);

        assertEquals(Set.of("42-Sample:https://example.org/.*"), resource.getServices());
        assertEquals(1, resource.getServiceCount());
        assertEquals(2, closed.get());
        verify(manager, never()).load();
    }

    @Test
    void verifyStreamClosedOnFailure() {
        val service = mock(RegisteredService.class);
        when(service.getName()).thenThrow(new IllegalStateException("Unavailable"));
        val closed = new AtomicBoolean();
        val manager = mock(ServicesManager.class);
        when(manager.stream()).thenAnswer(_ -> Stream.of(service).onClose(() -> closed.set(true)));

        assertThrows(IllegalStateException.class, () -> new ServicesManagerManagedResource(manager).getServices());
        assertTrue(closed.get());
    }

    @Test
    void verifyLookupAndReload() {
        val manager = mock(ServicesManager.class);
        val service = new CasRegisteredService();
        service.setId(42);
        service.setName("Sample");
        service.setServiceId("https://example.org/.*");
        when(manager.findServiceBy(42)).thenReturn(service);
        when(manager.load()).thenReturn(List.of(service));
        val resource = new ServicesManagerManagedResource(manager);

        assertEquals("42-Sample:https://example.org/.*", resource.getService(42));
        assertEquals(StringUtils.EMPTY, resource.getService(99));
        assertEquals(1, resource.reload());
        verify(manager).load();
    }
}
