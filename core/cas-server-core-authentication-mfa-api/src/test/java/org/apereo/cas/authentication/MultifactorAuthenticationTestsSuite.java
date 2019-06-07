package org.apereo.cas.authentication;

import org.apereo.cas.authentication.mfa.DefaultChainingMultifactorAuthenticationProviderTests;
import org.apereo.cas.authentication.mfa.DefaultMultifactorAuthenticationContextValidatorTests;
import org.apereo.cas.authentication.mfa.DefaultRequestedAuthenticationContextValidatorTests;
import org.apereo.cas.authentication.mfa.MultifactorAuthenticationProviderBypassTests;
import org.apereo.cas.authentication.mfa.bypass.DefaultChainingMultifactorAuthenticationBypassProviderTests;
import org.apereo.cas.authentication.mfa.bypass.audit.MultifactorAuthenticationProviderBypassAuditResourceResolverTests;
import org.apereo.cas.authentication.mfa.trigger.AdaptiveMultifactorAuthenticationTriggerTests;
import org.apereo.cas.authentication.mfa.trigger.AuthenticationAttributeMultifactorAuthenticationTriggerTests;
import org.apereo.cas.authentication.mfa.trigger.GlobalMultifactorAuthenticationTriggerTests;
import org.apereo.cas.authentication.mfa.trigger.GroovyScriptMultifactorAuthenticationTriggerTests;
import org.apereo.cas.authentication.mfa.trigger.HttpRequestMultifactorAuthenticationTriggerTests;
import org.apereo.cas.authentication.mfa.trigger.PrincipalAttributeMultifactorAuthenticationTriggerTests;
import org.apereo.cas.authentication.mfa.trigger.RegisteredServiceMultifactorAuthenticationTriggerTests;
import org.apereo.cas.authentication.mfa.trigger.RegisteredServicePrincipalAttributeMultifactorAuthenticationTriggerTests;
import org.apereo.cas.authentication.mfa.trigger.RestEndpointMultifactorAuthenticationTriggerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
    HttpRequestMultifactorAuthenticationTriggerTests.class,
    MultifactorAuthenticationProviderBypassAuditResourceResolverTests.class,
    GroovyScriptMultifactorAuthenticationTriggerTests.class,
    PrincipalAttributeMultifactorAuthenticationTriggerTests.class,
    DefaultChainingMultifactorAuthenticationBypassProviderTests.class,
    DefaultChainingMultifactorAuthenticationProviderTests.class
})
@RunWith(JUnitPlatform.class)
public class MultifactorAuthenticationTestsSuite {
}
