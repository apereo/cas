package org.apereo.cas.config;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintExtractor;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        "cas.authn.mfa.trusted.device-fingerprint.browser.enabled=true",

        "cas.authn.mfa.trusted.device-fingerprint.cookie.enabled=true",
        "cas.authn.mfa.trusted.device-fingerprint.cookie.crypto.enabled=false",
        "cas.authn.mfa.trusted.device-fingerprint.cookie.crypto.alg=" + ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256,
        "cas.authn.mfa.trusted.device-fingerprint.cookie.crypto.encryption.key=3RXtt06xYUAli7uU-Z915ZGe0MRBFw3uDjWgOEf1GT8",
        "cas.authn.mfa.trusted.device-fingerprint.cookie.crypto.signing.key=jIFR-fojN0vOIUcT0hDRXHLVp07CV-YeU8GnjICsXpu65lfkJbiKP028pT74Iurkor38xDGXNcXk_Y1V4rNDqw"
    })
@Tag("MFATrustedDevices")
@ExtendWith(CasTestExtension.class)
class MultifactorAuthnTrustedDeviceFingerprintConfigurationTests {
    @Autowired
    @Qualifier(DeviceFingerprintStrategy.DEFAULT_BEAN_NAME)
    private DeviceFingerprintStrategy deviceFingerprintStrategy;

    @Autowired
    @Qualifier("deviceFingerprintClientIpComponentExtractor")
    private DeviceFingerprintExtractor deviceFingerprintClientIpComponentExtractor;

    @Autowired
    @Qualifier("deviceFingerprintGeoLocationComponentExtractor")
    private DeviceFingerprintExtractor deviceFingerprintGeoLocationComponentExtractor;

    @Autowired
    @Qualifier("deviceFingerprintUserAgentComponentExtractor")
    private DeviceFingerprintExtractor deviceFingerprintUserAgentComponentExtractor;

    @Test
    void verifyOperation() {
        assertNotNull(deviceFingerprintClientIpComponentExtractor);
        assertNotNull(deviceFingerprintGeoLocationComponentExtractor);
        assertNotNull(deviceFingerprintUserAgentComponentExtractor);
        assertEquals(5, deviceFingerprintStrategy.getDeviceFingerprintExtractors().size());
    }
}
