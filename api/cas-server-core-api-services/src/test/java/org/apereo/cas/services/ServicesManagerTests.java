package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ServicesManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
public class ServicesManagerTests {
    @Test
    public void verifyFindById() {
        val component = mock(ServicesManager.class);
        val service = mock(RegisteredService.class);
        when(component.findServiceBy(anyLong())).thenReturn(service);
        when(component.findServiceBy(anyLong(), any())).thenCallRealMethod();
        assertNotNull(component.findServiceBy(1, RegisteredService.class));
    }

    @Test
    public void verifyFindByName() {
        val component = mock(ServicesManager.class);

        when(component.findServiceByName(anyString())).thenReturn(null);
        when(component.findServiceByName(anyString(), any())).thenCallRealMethod();
        assertNull(component.findServiceByName("test", BaseMockRegisteredService.class));

        val service = mock(RegisteredService.class);
        when(component.findServiceByName(anyString())).thenReturn(service);
        assertNotNull(component.findServiceByName("test", RegisteredService.class));
    }


    private abstract static class BaseMockRegisteredService implements RegisteredService {
        private static final long serialVersionUID = 5470970585502265482L;
    }
}
