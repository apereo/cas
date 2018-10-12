package org.apereo.cas.authentication;

import org.apereo.cas.authentication.mfa.DefaultMultifactorAuthenticationContextValidatorTests;
import org.apereo.cas.authentication.mfa.DefaultMultifactorAuthenticationProviderBypassTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link MultifactorAuthenticationTestSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    DefaultMultifactorAuthenticationContextValidatorTests.class,
    DefaultMultifactorAuthenticationProviderBypassTests.class
})
public class MultifactorAuthenticationTestSuite {
}
