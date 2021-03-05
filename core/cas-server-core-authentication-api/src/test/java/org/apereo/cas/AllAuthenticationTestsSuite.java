package org.apereo.cas;

import org.apereo.cas.authentication.AcceptAnyAuthenticationPolicyFactoryTests;
import org.apereo.cas.authentication.AuthenticationCredentialTypeMetaDataPopulatorTests;
import org.apereo.cas.authentication.AuthenticationDateAttributeMetaDataPopulatorTests;
import org.apereo.cas.authentication.AuthenticationTransactionTests;
import org.apereo.cas.authentication.CredentialCustomFieldsAttributeMetaDataPopulatorTests;
import org.apereo.cas.authentication.DefaultAuthenticationBuilderTests;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResultTests;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilderFactoryTests;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilderTests;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlanTests;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategyTests;
import org.apereo.cas.authentication.DefaultPrincipalFactoryTests;
import org.apereo.cas.authentication.GroovyAuthenticationPostProcessorTests;
import org.apereo.cas.authentication.GroovyAuthenticationPreProcessorTests;
import org.apereo.cas.authentication.GroovyPrincipalFactoryTests;
import org.apereo.cas.authentication.OneTimeTokenAccountTests;
import org.apereo.cas.authentication.OneTimeTokenTests;
import org.apereo.cas.authentication.RequiredHandlerAuthenticationPolicyFactoryTests;
import org.apereo.cas.authentication.RestfulPrincipalFactoryTests;
import org.apereo.cas.authentication.adaptive.DefaultAdaptiveAuthenticationPolicyTests;
import org.apereo.cas.authentication.adaptive.intel.BlackDotIPAddressIntelligenceServiceTests;
import org.apereo.cas.authentication.adaptive.intel.DefaultIPAddressIntelligenceServiceTests;
import org.apereo.cas.authentication.adaptive.intel.GroovyIPAddressIntelligenceServiceTests;
import org.apereo.cas.authentication.adaptive.intel.RestfulIPAddressIntelligenceServiceTests;
import org.apereo.cas.authentication.credential.CredentialTests;
import org.apereo.cas.authentication.exceptions.UniquePrincipalRequiredExceptionTests;
import org.apereo.cas.authentication.handler.ByCredentialSourceAuthenticationHandlerResolverTests;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolverTests;
import org.apereo.cas.authentication.metadata.SuccessfulHandlerMetaDataPopulatorTests;
import org.apereo.cas.authentication.policy.ExcludedAuthenticationHandlerAuthenticationPolicyTests;
import org.apereo.cas.authentication.policy.GroovyScriptAuthenticationPolicyTests;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtilsTests;
import org.apereo.cas.authentication.principal.resolvers.EchoingPrincipalResolverTests;
import org.apereo.cas.authentication.principal.resolvers.InternalGroovyScriptDaoTests;
import org.apereo.cas.authentication.principal.resolvers.NullPrincipalTests;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolverTests;
import org.apereo.cas.authentication.principal.resolvers.ProxyingPrincipalResolverTests;
import org.apereo.cas.authentication.support.password.DefaultPasswordPolicyHandlingStrategyTests;
import org.apereo.cas.authentication.support.password.GroovyPasswordEncoderTests;
import org.apereo.cas.authentication.support.password.PasswordExpiringWarningMessageDescriptorTests;
import org.apereo.cas.authentication.support.password.RejectResultCodePasswordPolicyHandlingStrategyTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllAuthenticationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    DefaultAdaptiveAuthenticationPolicyTests.class,
    GroovyIPAddressIntelligenceServiceTests.class,
    RestfulIPAddressIntelligenceServiceTests.class,
    BlackDotIPAddressIntelligenceServiceTests.class,
    GroovyScriptAuthenticationPolicyTests.class,
    InternalGroovyScriptDaoTests.class,
    PersonDirectoryPrincipalResolverTests.class,
    PrincipalNameTransformerUtilsTests.class,
    AuthenticationCredentialTypeMetaDataPopulatorTests.class,
    DefaultPrincipalFactoryTests.class,
    GroovyAuthenticationPreProcessorTests.class,
    GroovyPrincipalFactoryTests.class,
    GroovyPasswordEncoderTests.class,
    DefaultAuthenticationBuilderTests.class,
    RestfulPrincipalFactoryTests.class,
    DefaultPasswordPolicyHandlingStrategyTests.class,
    RejectResultCodePasswordPolicyHandlingStrategyTests.class,
    PasswordExpiringWarningMessageDescriptorTests.class,
    OneTimeTokenAccountTests.class,
    CredentialTests.class,
    AuthenticationTransactionTests.class,
    DefaultAuthenticationResultBuilderFactoryTests.class,
    ExcludedAuthenticationHandlerAuthenticationPolicyTests.class,
    NullPrincipalTests.class,
    EchoingPrincipalResolverTests.class,
    DefaultAuthenticationHandlerExecutionResultTests.class,
    OneTimeTokenTests.class,
    SuccessfulHandlerMetaDataPopulatorTests.class,
    UniquePrincipalRequiredExceptionTests.class,
    DefaultAuthenticationServiceSelectionPlanTests.class,
    AcceptAnyAuthenticationPolicyFactoryTests.class,
    RequiredHandlerAuthenticationPolicyFactoryTests.class,
    DefaultIPAddressIntelligenceServiceTests.class,
    DefaultAuthenticationServiceSelectionStrategyTests.class,
    ProxyingPrincipalResolverTests.class,
    CredentialCustomFieldsAttributeMetaDataPopulatorTests.class,
    AuthenticationDateAttributeMetaDataPopulatorTests.class,
    ByCredentialTypeAuthenticationHandlerResolverTests.class,
    ByCredentialSourceAuthenticationHandlerResolverTests.class,
    DefaultAuthenticationResultBuilderTests.class,
    GroovyAuthenticationPostProcessorTests.class
})
@RunWith(JUnitPlatform.class)
public class AllAuthenticationTestsSuite {
}
