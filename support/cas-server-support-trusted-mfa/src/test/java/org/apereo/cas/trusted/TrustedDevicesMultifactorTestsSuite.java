package org.apereo.cas.trusted;

import org.apereo.cas.trusted.authentication.storage.InMemoryMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.storage.JsonMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.storage.MultifactorAuthenticationTrustStorageCleanerTests;
import org.apereo.cas.trusted.authentication.storage.fingerprint.ClientIpDeviceFingerprintComponentExtractorTests;
import org.apereo.cas.trusted.authentication.storage.fingerprint.DefaultDeviceFingerprintStrategyTests;
import org.apereo.cas.trusted.authentication.storage.fingerprint.UserAgentDeviceFingerprintComponentExtractorTests;
import org.apereo.cas.trusted.web.flow.MultifactorAuthenticationVerifyTrustActionTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link TrustedDevicesMultifactorTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    ClientIpDeviceFingerprintComponentExtractorTests.class,
    UserAgentDeviceFingerprintComponentExtractorTests.class,
    JsonMultifactorAuthenticationTrustStorageTests.class,
    InMemoryMultifactorAuthenticationTrustStorageTests.class,
    MultifactorAuthenticationVerifyTrustActionTests.class,
    DefaultDeviceFingerprintStrategyTests.class,
    MultifactorAuthenticationTrustStorageCleanerTests.class
})
@RunWith(JUnitPlatform.class)
public class TrustedDevicesMultifactorTestsSuite {
}
