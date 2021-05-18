package org.apereo.cas;

import org.apereo.cas.authentication.MultifactorAuthenticationCredentialTests;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluatorTests;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolverTests;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerTests;
import org.apereo.cas.authentication.bypass.ChainingMultifactorAuthenticationProviderBypassEvaluatorTests;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluatorTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * The {@link AllTestsSuite} is responsible for
 * running all cas test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    MultifactorAuthenticationCredentialTests.class,
    MultifactorAuthenticationProviderBypassEvaluatorTests.class,
    ChainingMultifactorAuthenticationProviderBypassEvaluatorTests.class,
    MultifactorAuthenticationFailureModeEvaluatorTests.class,
    MultifactorAuthenticationPrincipalResolverTests.class,
    MultifactorAuthenticationTriggerTests.class
})
@Suite
public class AllTestsSuite {
}
