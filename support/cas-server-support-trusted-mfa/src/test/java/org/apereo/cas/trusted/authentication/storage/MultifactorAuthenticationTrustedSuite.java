package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.authentication.storage.fingerprint.ClientIpDeviceFingerprintComponentExtractorTests;
import org.apereo.cas.trusted.authentication.storage.fingerprint.UserAgentDeviceFingerprintComponentExtractorTests;

import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link MultifactorAuthenticationTrustedSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    InMemoryMultifactorAuthenticationTrustStorageTests.class,
    UserAgentDeviceFingerprintComponentExtractorTests.class,
    JsonMultifactorAuthenticationTrustStorageTests.class,
    ClientIpDeviceFingerprintComponentExtractorTests.class
})
@Slf4j
public class MultifactorAuthenticationTrustedSuite {
}
