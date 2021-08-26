package org.apereo.cas;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountCipherExecutorTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyMultifactorAuthenticationProviderTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyRestHttpRequestCredentialFactoryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllYubiKeyTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    YubiKeyAccountCipherExecutorTests.class,
    YubiKeyMultifactorAuthenticationProviderTests.class,
    YubiKeyRestHttpRequestCredentialFactoryTests.class
})
@Suite
public class AllYubiKeyTestsSuite {
}
