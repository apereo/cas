package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.adaptors.yubikey.BaseYubiKeyTests;
import org.apereo.cas.authentication.device.MultifactorAuthenticationRegisteredDevice;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link YubiKeyMultifactorAuthenticationDeviceProviderActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@SpringBootTest(classes = BaseYubiKeyTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.yubikey.client-id=18423",
        "cas.authn.mfa.yubikey.secret-key=zAIqhjui12mK8x82oe9qzBEb0As=",
        "cas.authn.mfa.yubikey.allowed-devices.casuseryubikey=device-identifier"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("WebflowEvents")
class YubiKeyMultifactorAuthenticationDeviceProviderActionTests {
    @Autowired
    @Qualifier("yubiKeyDeviceProviderAction")
    private MultifactorAuthenticationDeviceProviderAction yubiKeyDeviceProviderAction;
    
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create();
        val authentication = RegisteredServiceTestUtils.getAuthentication("casuseryubikey");
        WebUtils.putAuthentication(authentication, context);
        assertNull(yubiKeyDeviceProviderAction.execute(context));
        val registeredDevices = (List<MultifactorAuthenticationRegisteredDevice>) WebUtils.getMultifactorAuthenticationRegisteredDevices(context);
        assertEquals(1, registeredDevices.size());

    }

}
