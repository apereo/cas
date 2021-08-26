package org.apereo.cas;

import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluatorTests;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderResolverTests;
import org.apereo.cas.authentication.bypass.AuthenticationMultifactorAuthenticationProviderBypassEvaluatorTests;
import org.apereo.cas.authentication.bypass.CredentialMultifactorAuthenticationProviderBypassEvaluatorTests;
import org.apereo.cas.authentication.bypass.HttpRequestMultifactorAuthenticationProviderBypassEvaluatorTests;
import org.apereo.cas.authentication.bypass.NeverAllowMultifactorAuthenticationProviderBypassEvaluatorTests;
import org.apereo.cas.authentication.bypass.PrincipalMultifactorAuthenticationProviderBypassEvaluatorTests;
import org.apereo.cas.authentication.bypass.RegisteredServiceMultifactorAuthenticationProviderBypassEvaluatorTests;
import org.apereo.cas.authentication.bypass.RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluatorTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    HttpRequestMultifactorAuthenticationProviderBypassEvaluatorTests.class,
    DefaultMultifactorAuthenticationProviderResolverTests.class,
    DefaultMultifactorAuthenticationFailureModeEvaluatorTests.class,
    RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluatorTests.class,
    NeverAllowMultifactorAuthenticationProviderBypassEvaluatorTests.class,
    AuthenticationMultifactorAuthenticationProviderBypassEvaluatorTests.class,
    CredentialMultifactorAuthenticationProviderBypassEvaluatorTests.class,
    PrincipalMultifactorAuthenticationProviderBypassEvaluatorTests.class,
    RegisteredServiceMultifactorAuthenticationProviderBypassEvaluatorTests.class
})
@Suite
public class AllTestsSuite {
}
