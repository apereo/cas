package org.apereo.cas.adaptors.yubikey;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllYubiKeyTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    YubiKeyMultifactorAuthenticationProviderTests.class,
    YubiKeyRestHttpRequestCredentialFactoryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllYubiKeyTestsSuite {
}
