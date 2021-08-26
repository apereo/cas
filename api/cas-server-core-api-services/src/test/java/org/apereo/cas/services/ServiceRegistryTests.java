package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
public class ServiceRegistryTests {
    @Test
    public void verifyFindById() {
        val component = mock(ServiceRegistry.class);
        val service = mock(RegisteredService.class);
        when(component.findServiceById(anyLong())).thenReturn(service);
        when(component.findServiceById(anyLong(), any())).thenCallRealMethod();
        assertThrows(ClassCastException.class,
            () -> component.findServiceById(1, BaseMockRegisteredService.class));
    }

    @Test
    public void verifyFindByName() {
        val component = mock(ServiceRegistry.class);

        when(component.findServiceByExactServiceName(anyString())).thenReturn(null);
        when(component.findServiceByExactServiceName(anyString(), any())).thenCallRealMethod();
        assertNull(component.findServiceByExactServiceName("test", BaseMockRegisteredService.class));

        val service = mock(RegisteredService.class);
        when(component.findServiceByExactServiceName(anyString())).thenReturn(service);
        assertThrows(ClassCastException.class,
            () -> component.findServiceByExactServiceName("test", BaseMockRegisteredService.class));
    }

    private abstract static class BaseMockRegisteredService implements RegisteredService {
        private static final long serialVersionUID = 5470970585502265482L;
    }
}
