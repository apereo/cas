package org.apereo.cas;

import org.apereo.cas.authentication.AuthenticationHandlerTests;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulatorTests;
import org.apereo.cas.authentication.AuthenticationPolicyExecutionResultTests;
import org.apereo.cas.authentication.AuthenticationPolicyTests;
import org.apereo.cas.authentication.AuthenticationPostProcessorTests;
import org.apereo.cas.authentication.AuthenticationPreProcessorTests;
import org.apereo.cas.authentication.PrincipalElectionStrategyTests;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationExceptionTests;
import org.apereo.cas.authentication.principal.PersistentIdGeneratorTests;
import org.apereo.cas.authentication.principal.ServiceMatchingStrategyTests;
import org.apereo.cas.authentication.principal.ServiceTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllAuthenticationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    UnauthorizedAuthenticationExceptionTests.class,
    AuthenticationHandlerTests.class,
    AuthenticationPolicyTests.class,
    ServiceTests.class,
    AuthenticationPolicyExecutionResultTests.class,
    PersistentIdGeneratorTests.class,
    ServiceMatchingStrategyTests.class,
    PrincipalElectionStrategyTests.class,
    AuthenticationMetaDataPopulatorTests.class,
    AuthenticationPreProcessorTests.class,
    AuthenticationPostProcessorTests.class
})
@Suite
public class AllAuthenticationTestsSuite {
}
