package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ClosedYubiKeyAccountRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFAProvider")
public class ClosedYubiKeyAccountRegistryTests {

    @Test
    public void verifyOperation() {
        val registry = new ClosedYubiKeyAccountRegistry(mock(YubiKeyAccountValidator.class));
        assertTrue(registry.getAccount("casuser").isEmpty());
        assertTrue(registry.getAccounts().isEmpty());
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                registry.delete("casuser");
                registry.delete("casuser", 12345);
                registry.deleteAll();
            }
        });
        assertNull(registry.save(YubiKeyDeviceRegistrationRequest.builder().build(),
            YubiKeyRegisteredDevice.builder().build()));
        assertFalse(registry.isYubiKeyRegisteredFor("casuser"));
        assertFalse(registry.isYubiKeyRegisteredFor("casuser", "publicId"));
        assertFalse(registry.registerAccountFor(YubiKeyDeviceRegistrationRequest.builder().build()));
        assertFalse(registry.update(YubiKeyAccount.builder().build()));
        assertNull(registry.save(YubiKeyAccount.builder().build()));
    }

}
