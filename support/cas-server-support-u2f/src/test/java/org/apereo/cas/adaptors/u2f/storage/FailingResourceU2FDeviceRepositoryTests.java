package org.apereo.cas.adaptors.u2f.storage;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link FailingResourceU2FDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("MFAProvider")
public class FailingResourceU2FDeviceRepositoryTests {

    @Test
    public void verifyOperation() throws Exception {
        val failure = mock(BaseResourceU2FDeviceRepository.class);
        when(failure.readDevicesFromResource()).thenThrow(new IllegalStateException());
        doThrow(new IllegalStateException()).when(failure).writeDevicesBackToResource(any());
        when(failure.getRegisteredDevices()).thenCallRealMethod();
        when(failure.getRegisteredDevices(anyString())).thenCallRealMethod();
        when(failure.registerDevice(any())).thenCallRealMethod();
        doCallRealMethod().when(failure).clean();
        doCallRealMethod().when(failure).deleteRegisteredDevice(any());

        assertTrue(failure.getRegisteredDevices().isEmpty());
        assertTrue(failure.getRegisteredDevices("user").isEmpty());
        assertNull(failure.registerDevice(U2FDeviceRegistration.builder().username("user").build()));
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                failure.deleteRegisteredDevice(U2FDeviceRegistration.builder().username("user").build());
                failure.clean();
            }
        });
    }
}
