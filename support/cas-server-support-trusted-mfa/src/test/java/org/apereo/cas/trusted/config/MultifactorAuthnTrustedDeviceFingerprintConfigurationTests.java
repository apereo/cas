package org.apereo.cas.trusted.config;

import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthnTrustedDeviceFingerprintConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(
    classes = AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.trusted.device-fingerprint.client-ip.enabled=true",
        "cas.authn.mfa.trusted.device-fingerprint.geolocation.enabled=true",
        "cas.authn.mfa.trusted.device-fingerprint.user-agent.enabled=true",
        "cas.authn.mfa.trusted.device-fingerprint.cookie.enabled=true"
    })
@Tag("MFA")
public class MultifactorAuthnTrustedDeviceFingerprintConfigurationTests {
    @Autowired
    @Qualifier("deviceFingerprintStrategy")
    private DeviceFingerprintStrategy deviceFingerprintStrategy;

    @Test
    public void verifyOperation() {
        assertEquals(4, deviceFingerprintStrategy.getDeviceFingerprintComponentExtractors().size());
    }
}
