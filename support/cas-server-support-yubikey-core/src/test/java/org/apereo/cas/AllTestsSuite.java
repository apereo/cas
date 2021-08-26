package org.apereo.cas;

import org.apereo.cas.adaptors.yubikey.AcceptAllYubiKeyAccountValidatorTests;
import org.apereo.cas.adaptors.yubikey.DefaultYubiKeyAccountValidatorTests;
import org.apereo.cas.adaptors.yubikey.DenyAllYubiKeyAccountValidatorTests;
import org.apereo.cas.adaptors.yubikey.registry.YubiKeyAccountRegistryTests;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAccountCheckRegistrationActionTests;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAccountSaveRegistrationActionTests;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAuthenticationPrepareLoginActionTests;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAuthenticationWebflowActionTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

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
    YubiKeyAuthenticationWebflowActionTests.class,
    YubiKeyAccountRegistryTests.class,
    YubiKeyAuthenticationPrepareLoginActionTests.class,
    AcceptAllYubiKeyAccountValidatorTests.class,
    DenyAllYubiKeyAccountValidatorTests.class
})
@Suite
public class AllTestsSuite {
}
