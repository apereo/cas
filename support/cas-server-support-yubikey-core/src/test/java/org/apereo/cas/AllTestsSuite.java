
package org.apereo.cas;

import org.apereo.cas.adaptors.yubikey.AcceptAllYubiKeyAccountValidatorTests;
import org.apereo.cas.adaptors.yubikey.DefaultYubiKeyAccountValidatorTests;
import org.apereo.cas.adaptors.yubikey.DenyAllYubiKeyAccountValidatorTests;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAccountCheckRegistrationActionTests;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAccountSaveRegistrationActionTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    YubiKeyAccountSaveRegistrationActionTests.class,
    YubiKeyAccountCheckRegistrationActionTests.class,
    DefaultYubiKeyAccountValidatorTests.class,
    AcceptAllYubiKeyAccountValidatorTests.class,
    DenyAllYubiKeyAccountValidatorTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
