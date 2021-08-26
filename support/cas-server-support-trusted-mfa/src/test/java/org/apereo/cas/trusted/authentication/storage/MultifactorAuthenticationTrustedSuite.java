package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.authentication.storage.fingerprint.ClientIpDeviceFingerprintComponentManagerTests;
import org.apereo.cas.trusted.authentication.storage.fingerprint.UserAgentDeviceFingerprintComponentManagerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link MultifactorAuthenticationTrustedSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    InMemoryMultifactorAuthenticationTrustStorageTests.class,
    UserAgentDeviceFingerprintComponentManagerTests.class,
    JsonMultifactorAuthenticationTrustStorageTests.class,
    ClientIpDeviceFingerprintComponentManagerTests.class
})
@Suite
public class MultifactorAuthenticationTrustedSuite {
}
