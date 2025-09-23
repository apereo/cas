package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link YubiKeyMultifactorAuthenticatorDeviceManager}.
 *
 * @author Misagh Moayyed
 * @since 7.2
 */
@SpringBootTest(classes = BaseYubiKeyTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.yubikey.client-id=18423",
        "cas.authn.mfa.yubikey.secret-key=zAIqhjui12mK8x82oe9qzBEb0As=",
        "cas.authn.mfa.yubikey.allowed-devices.casuser=deviceid"
    })
@Tag("MFAProvider")
@ExtendWith(CasTestExtension.class)
class YubiKeyMultifactorAuthenticatorDeviceManagerTests {
    @Autowired
    @Qualifier("yubikeyMultifactorAuthenticationProvider")
    private MultifactorAuthenticationProvider yubikeyMultifactorAuthenticationProvider;

    @Test
    void verifyOperation() {
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser");
        val devices = yubikeyMultifactorAuthenticationProvider.getDeviceManager()
            .findRegisteredDevices(principal);
        assertEquals(1, devices.size());
        assertTrue(yubikeyMultifactorAuthenticationProvider.getDeviceManager().hasRegisteredDevices(principal));

        val device = devices.getFirst();
        yubikeyMultifactorAuthenticationProvider.getDeviceManager().removeRegisteredDevice(principal, device.getId());
        assertFalse(yubikeyMultifactorAuthenticationProvider.getDeviceManager().hasRegisteredDevices(principal));
    }
}
