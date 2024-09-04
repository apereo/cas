package org.apereo.cas.web.report;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationDevicesEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = "management.endpoint.mfaDevices.enabled=true")
@Tag("ActuatorEndpoint")
class MultifactorAuthenticationDevicesEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("multifactorAuthenticationDevicesEndpoint")
    private MultifactorAuthenticationDevicesEndpoint mfaDevicesEndpoint;

    @Test
    void verifyOperation() throws Throwable {
        val results = mfaDevicesEndpoint.allMfaDevicesForUser("casuser");
        assertTrue(results.isEmpty());
    }
}
