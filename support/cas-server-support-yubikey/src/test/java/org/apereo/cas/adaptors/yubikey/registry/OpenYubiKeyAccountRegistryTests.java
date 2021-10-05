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
 * This is {@link OpenYubiKeyAccountRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFAProvider")
public class OpenYubiKeyAccountRegistryTests {

    @Test
    public void verifyOperation() {
        val registry = new OpenYubiKeyAccountRegistry(mock(YubiKeyAccountValidator.class));
        assertFalse(registry.getAccount("casuser").isEmpty());
        assertTrue(registry.getAccounts().isEmpty());
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                registry.delete("casuser");
                registry.delete("casuser", 12345);
                registry.deleteAll();
            }
        });
        assertNotNull(registry.save(YubiKeyDeviceRegistrationRequest.builder().build(),
            YubiKeyRegisteredDevice.builder().build()));
        assertTrue(registry.isYubiKeyRegisteredFor("casuser"));
        assertTrue(registry.isYubiKeyRegisteredFor("casuser", "publicId"));
        assertTrue(registry.registerAccountFor(YubiKeyDeviceRegistrationRequest.builder().build()));
        assertTrue(registry.update(YubiKeyAccount.builder().build()));
        assertNotNull(registry.save(YubiKeyAccount.builder().build()));
    }

}
