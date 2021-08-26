package org.apereo.cas;

import org.apereo.cas.adaptors.duo.DefaultDuoSecurityMultifactorAuthenticationProviderTests;
import org.apereo.cas.adaptors.duo.DuoSecurityHealthIndicatorTests;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationResultTests;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityUniversalPromptCredentialTests;
import org.apereo.cas.adaptors.duo.rest.DuoSecurityRestHttpRequestCredentialFactoryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllDuoSecurityTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */

@SelectClasses({
    DefaultDuoSecurityMultifactorAuthenticationProviderTests.class,
    DuoSecurityAuthenticationResultTests.class,
    DuoSecurityUniversalPromptCredentialTests.class,
    DuoSecurityRestHttpRequestCredentialFactoryTests.class,
    DuoSecurityHealthIndicatorTests.class
})
@Suite
public class AllDuoSecurityTestsSuite {
}
