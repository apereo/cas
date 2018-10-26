package org.apereo.cas.authentication;

import org.apereo.cas.authentication.mfa.DefaultMultifactorAuthenticationContextValidatorTests;
import org.apereo.cas.authentication.mfa.DefaultMultifactorAuthenticationProviderBypassTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link MultifactorAuthenticationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    DefaultMultifactorAuthenticationContextValidatorTests.class,
    DefaultMultifactorAuthenticationProviderBypassTests.class,
    GroovyMultifactorAuthenticationProviderBypassTests.class,
    DefaultMultifactorTriggerSelectionStrategyTests.class,
    RestMultifactorAuthenticationProviderBypassTests.class
})
public class MultifactorAuthenticationTestsSuite {
}
