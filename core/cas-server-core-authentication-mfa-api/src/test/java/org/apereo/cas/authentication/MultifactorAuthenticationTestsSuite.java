package org.apereo.cas.authentication;

import org.apereo.cas.authentication.mfa.DefaultMultifactorAuthenticationContextValidatorTests;
import org.apereo.cas.authentication.mfa.MultifactorAuthenticationProviderBypassTests;
import org.apereo.cas.authentication.mfa.trigger.AdaptiveMultifactorAuthenticationTriggerTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link MultifactorAuthenticationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    DefaultMultifactorAuthenticationContextValidatorTests.class,
    MultifactorAuthenticationProviderBypassTests.class,
    GroovyMultifactorAuthenticationProviderBypassTests.class,
    DefaultMultifactorTriggerSelectionStrategyTests.class,
    RestMultifactorAuthenticationProviderBypassTests.class,
    AdaptiveMultifactorAuthenticationTriggerTests.class
})
public class MultifactorAuthenticationTestsSuite {
}
