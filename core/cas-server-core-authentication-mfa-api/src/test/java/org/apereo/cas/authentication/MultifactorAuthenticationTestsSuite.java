package org.apereo.cas.authentication;

import org.apereo.cas.authentication.mfa.DefaultMultifactorAuthenticationContextValidatorTests;
import org.apereo.cas.authentication.mfa.DefaultRequestedAuthenticationContextValidatorTests;
import org.apereo.cas.authentication.mfa.MultifactorAuthenticationProviderBypassTests;
import org.apereo.cas.authentication.mfa.trigger.AdaptiveMultifactorAuthenticationTriggerTests;
import org.apereo.cas.authentication.mfa.trigger.AuthenticationAttributeMultifactorAuthenticationTriggerTests;
import org.apereo.cas.authentication.mfa.trigger.GlobalMultifactorAuthenticationTriggerTests;
import org.apereo.cas.authentication.mfa.trigger.HttpRequestMultifactorAuthenticationTriggerTests;
import org.apereo.cas.authentication.mfa.trigger.RegisteredServiceMultifactorAuthenticationTriggerTests;
import org.apereo.cas.authentication.mfa.trigger.RegisteredServicePrincipalAttributeMultifactorAuthenticationTriggerTests;
import org.apereo.cas.authentication.mfa.trigger.RestEndpointMultifactorAuthenticationTriggerTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link MultifactorAuthenticationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    DefaultMultifactorAuthenticationContextValidatorTests.class,
    DefaultRequestedAuthenticationContextValidatorTests.class,
    MultifactorAuthenticationProviderBypassTests.class,
    GroovyMultifactorAuthenticationProviderBypassTests.class,
    DefaultMultifactorTriggerSelectionStrategyTests.class,
    RestMultifactorAuthenticationProviderBypassTests.class,
    AdaptiveMultifactorAuthenticationTriggerTests.class,
    GlobalMultifactorAuthenticationTriggerTests.class,
    RegisteredServiceMultifactorAuthenticationTriggerTests.class,
    RegisteredServicePrincipalAttributeMultifactorAuthenticationTriggerTests.class,
    AuthenticationAttributeMultifactorAuthenticationTriggerTests.class,
    RestEndpointMultifactorAuthenticationTriggerTests.class,
    HttpRequestMultifactorAuthenticationTriggerTests.class
})
public class MultifactorAuthenticationTestsSuite {
}
