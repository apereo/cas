package org.apereo.cas.trusted.web;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.web.report.AbstractCasEndpointTests;

import lombok.val;
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
@TestPropertySource(properties = "management.endpoint.multifactorTrustedDevices.enabled=true")
@Tag("MFA")
@ImportAutoConfiguration(MultifactorAuthnTrustConfiguration.class)
public class MultifactorTrustedDevicesReportEndpointTests extends AbstractCasEndpointTests {

    @Autowired
    @Qualifier("mfaTrustedDevicesReportEndpoint")
    private MultifactorTrustedDevicesReportEndpoint endpoint;

    @Autowired
    @Qualifier("mfaTrustEngine")
    private MultifactorAuthenticationTrustStorage mfaTrustEngine;

    @Test
    public void verifyOperation() {
        assertNotNull(endpoint);
        val record = mfaTrustEngine.set(MultifactorAuthenticationTrustRecord.newInstance("casuser",
            "geography", "fingerprint"));
        assertFalse(endpoint.devices().isEmpty());
        assertFalse(endpoint.devicesForUser("casuser").isEmpty());

        endpoint.revoke(record.getRecordKey());
        assertTrue(endpoint.devices().isEmpty());
        assertTrue(endpoint.devicesForUser("casuser").isEmpty());
    }
}
