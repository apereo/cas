package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.authentication.storage.fingerprint.ClientIpDeviceFingerprintComponentExtractorTests;
import org.apereo.cas.trusted.authentication.storage.fingerprint.UserAgentDeviceFingerprintComponentExtractorTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link MultifactorAuthenticationTrustedSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    InMemoryMultifactorAuthenticationTrustStorageTests.class,
    UserAgentDeviceFingerprintComponentExtractorTests.class,
    JsonMultifactorAuthenticationTrustStorageTests.class,
    ClientIpDeviceFingerprintComponentExtractorTests.class
})
public class MultifactorAuthenticationTrustedSuite {
}
