package org.apereo.cas;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The {@link AllCoreTestsSuite} is responsible for
 * running all cas test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    DefaultCentralAuthenticationServiceTests.class,
    DefaultCentralAuthenticationServiceMockitoTests.class,
    DefaultCasAttributeEncoderTests.class,
    AdaptiveMultifactorAuthenticationPolicyEventResolverTests.class,
    DefaultPrincipalAttributesRepositoryTests.class,
    GroovyScriptMultifactorAuthenticationPolicyEventResolverTests.class,
    TimedMultifactorAuthenticationPolicyEventResolverTests.class,
    MultifactorAuthenticationTests.class,
    RequestHeaderMultifactorAuthenticationPolicyEventResolverTests.class,
    RequestSessionAttributeMultifactorAuthenticationPolicyEventResolverTests.class,
    AuthenticationAttributeMultifactorAuthenticationPolicyEventResolverTests.class,
    RequestParameterMultifactorAuthenticationPolicyEventResolverTests.class
})
public class AllCoreTestsSuite {
}
