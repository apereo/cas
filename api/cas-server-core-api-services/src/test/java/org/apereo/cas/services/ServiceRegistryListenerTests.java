package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ServiceRegistryListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
public class ServiceRegistryListenerTests {
    @Test
    public void verifyOperation() {
        val listener = ServiceRegistryListener.noOp();
        assertNotNull(listener.postLoad(mock(RegisteredService.class)));
        assertNotNull(listener.preSave(mock(RegisteredService.class)));
        assertEquals(0, listener.getOrder());
    }

}
