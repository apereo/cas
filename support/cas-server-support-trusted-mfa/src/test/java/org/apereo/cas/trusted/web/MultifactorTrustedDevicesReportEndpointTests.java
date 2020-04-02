package org.apereo.cas.trusted.web;

import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.web.report.AbstractCasEndpointTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorTrustedDevicesReportEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = {
    "management.endpoint.multifactorTrustedDevices.enabled=true"
})
@Tag("MFA")
@ImportAutoConfiguration(MultifactorAuthnTrustConfiguration.class)
public class MultifactorTrustedDevicesReportEndpointTests extends AbstractCasEndpointTests {

    @Autowired
    @Qualifier("mfaTrustedDevicesReportEndpoint")
    private MultifactorTrustedDevicesReportEndpoint endpoint;

    @Test
    public void verifyOperation() {
        assertNotNull(endpoint);
    }
}
