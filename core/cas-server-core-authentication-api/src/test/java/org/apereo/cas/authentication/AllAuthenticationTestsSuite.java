package org.apereo.cas.authentication;

import org.apereo.cas.authentication.adaptive.DefaultAdaptiveAuthenticationPolicyTests;
import org.apereo.cas.authentication.adaptive.intel.BlackDotIPAddressIntelligenceServiceTests;
import org.apereo.cas.authentication.adaptive.intel.GroovyIPAddressIntelligenceServiceTests;
import org.apereo.cas.authentication.adaptive.intel.RestfulIPAddressIntelligenceServiceTests;
import org.apereo.cas.authentication.policy.GroovyScriptAuthenticationPolicyTests;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtilsTests;
import org.apereo.cas.authentication.principal.resolvers.InternalGroovyScriptDaoTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllAuthenticationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    DefaultAdaptiveAuthenticationPolicyTests.class,
    GroovyIPAddressIntelligenceServiceTests.class,
    RestfulIPAddressIntelligenceServiceTests.class,
    BlackDotIPAddressIntelligenceServiceTests.class,
    GroovyScriptAuthenticationPolicyTests.class,
    InternalGroovyScriptDaoTests.class,
    PrincipalNameTransformerUtilsTests.class,
    AuthenticationCredentialTypeMetaDataPopulatorTests.class,
    DefaultPrincipalFactoryTests.class,
    GroovyAuthenticationPreProcessorTests.class,
    GroovyPrincipalFactoryTests.class,
    OneTimeTokenAccountTests.class
})
public class AllAuthenticationTestsSuite {
}
