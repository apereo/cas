package org.apereo.cas.qr.web;

import module java.base;
import org.apereo.cas.qr.BaseQRAuthenticationTokenValidatorServiceTests;
import org.apereo.cas.test.CasTestExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link QRAuthenticationDeviceRepositoryEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("ActuatorEndpoint")
@SpringBootTest(classes = BaseQRAuthenticationTokenValidatorServiceTests.SharedTestConfiguration.class,
    properties = {
        "management.endpoint.qrDevices.access=UNRESTRICTED",
        "management.endpoints.web.exposure.include=*"
    })
@ExtendWith(CasTestExtension.class)
class QRAuthenticationDeviceRepositoryEndpointTests {
    @Autowired
    @Qualifier("qrAuthenticationDeviceRepositoryEndpoint")
    private QRAuthenticationDeviceRepositoryEndpoint qrAuthenticationDeviceRepositoryEndpoint;

    @Test
    void verifyOperation() {
        assertTrue(qrAuthenticationDeviceRepositoryEndpoint.devices("casuser").isEmpty());
        assertDoesNotThrow(() -> {
            qrAuthenticationDeviceRepositoryEndpoint.registerDevice("casuser", UUID.randomUUID().toString());
            qrAuthenticationDeviceRepositoryEndpoint.removeDevice(UUID.randomUUID().toString());
        });
    }

}
