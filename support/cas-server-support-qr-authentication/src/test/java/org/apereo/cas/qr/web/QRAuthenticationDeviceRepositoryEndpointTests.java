package org.apereo.cas.qr.web;

import org.apereo.cas.qr.BaseQRAuthenticationTokenValidatorServiceTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

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
        "management.endpoint.qrDevices.enabled=true",
        "management.endpoints.web.exposure.include=*"
    })
public class QRAuthenticationDeviceRepositoryEndpointTests {
    @Autowired
    @Qualifier("qrAuthenticationDeviceRepositoryEndpoint")
    private QRAuthenticationDeviceRepositoryEndpoint qrAuthenticationDeviceRepositoryEndpoint;

    @Test
    public void verifyOperation() {
        assertTrue(qrAuthenticationDeviceRepositoryEndpoint.devices("casuser").isEmpty());
        assertDoesNotThrow(() -> {
            qrAuthenticationDeviceRepositoryEndpoint.registerDevice("casuser", UUID.randomUUID().toString());
            qrAuthenticationDeviceRepositoryEndpoint.removeDevice(UUID.randomUUID().toString());
        });
    }

}
