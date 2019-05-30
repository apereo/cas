package org.apereo.cas.authentication;

import org.apereo.cas.authentication.adaptive.DefaultAdaptiveAuthenticationPolicyTests;
import org.apereo.cas.authentication.adaptive.intel.BlackDotIPAddressIntelligenceServiceTests;
import org.apereo.cas.authentication.adaptive.intel.GroovyIPAddressIntelligenceServiceTests;
import org.apereo.cas.authentication.adaptive.intel.RestfulIPAddressIntelligenceServiceTests;
import org.apereo.cas.authentication.handler.ByCredentialSourceAuthenticationHandlerResolverTests;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolverTests;
import org.apereo.cas.authentication.policy.GroovyScriptAuthenticationPolicyTests;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtilsTests;
import org.apereo.cas.authentication.principal.resolvers.InternalGroovyScriptDaoTests;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolverTests;
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
    DefaultPasswordPolicyHandlingStrategyTests.class,
    RejectResultCodePasswordPolicyHandlingStrategyTests.class,
    PasswordExpiringWarningMessageDescriptorTests.class,
    OneTimeTokenAccountTests.class,
    ByCredentialTypeAuthenticationHandlerResolverTests.class,
    ByCredentialSourceAuthenticationHandlerResolverTests.class,
    DefaultAuthenticationResultBuilderTests.class,
    GroovyAuthenticationPostProcessorTests.class
})
@RunWith(JUnitPlatform.class)
public class AllAuthenticationTestsSuite {
}
