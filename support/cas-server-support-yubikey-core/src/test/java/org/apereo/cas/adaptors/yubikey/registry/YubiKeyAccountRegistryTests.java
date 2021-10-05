package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link YubiKeyAccountRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFAProvider")
public class YubiKeyAccountRegistryTests {

    @Test
    public void verifyAuthz() {
        val registry = mock(BaseYubiKeyAccountRegistry.class);
        when(registry.isYubiKeyRegisteredFor(anyString(), anyString())).thenCallRealMethod();
        when(registry.isYubiKeyRegisteredFor(anyString())).thenCallRealMethod();
        when(registry.getAccount(anyString())).thenThrow(new RuntimeException());

        assertFalse(registry.isYubiKeyRegisteredFor("cas"));
        assertFalse(registry.isYubiKeyRegisteredFor("cas", "device"));
    }

    @Test
    public void verifyAcct() {
        val registry = mock(BaseYubiKeyAccountRegistry.class);
        when(registry.getAccount(anyString())).thenCallRealMethod();
        when(registry.getAccountInternal(anyString())).thenThrow(new RuntimeException());
        assertFalse(registry.getAccount("cas").isPresent());
    }

    @Test
    public void verifyInvalidAcct() {
        val registeredDevice = YubiKeyRegisteredDevice.builder()
            .id(System.currentTimeMillis())
            .name("first-device")
            .publicId("bad-id")
            .registrationDate(ZonedDateTime.now(Clock.systemUTC()))
            .build();

        val account = YubiKeyAccount.builder()
            .devices(CollectionUtils.wrapList(registeredDevice))
            .username("casuser")
            .build();

        val registry = mock(BaseYubiKeyAccountRegistry.class);
        when(registry.getAccount(anyString())).thenCallRealMethod();
        when(registry.getCipherExecutor()).thenThrow(new RuntimeException());
        when(registry.getAccountInternal(anyString())).thenReturn(account);

        val result = registry.getAccount("cas");
        assertTrue(result.isPresent());
        assertTrue(result.get().getDevices().isEmpty());
    }

}
