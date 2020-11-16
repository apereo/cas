package org.apereo.cas;

import org.apereo.cas.adaptors.duo.DefaultDuoSecurityMultifactorAuthenticationProviderTests;
import org.apereo.cas.adaptors.duo.DuoSecurityHealthIndicatorTests;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationResultTests;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationServiceTests;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityUniversalPromptCredentialTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllDuoSecurityTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */

@SelectClasses({
    DefaultDuoSecurityMultifactorAuthenticationProviderTests.class,
    DuoSecurityAuthenticationResultTests.class,
    DuoSecurityAuthenticationServiceTests.class,
    DuoSecurityUniversalPromptCredentialTests.class,
    DuoSecurityHealthIndicatorTests.class
})
@RunWith(JUnitPlatform.class)
public class AllDuoSecurityTestsSuite {
}
