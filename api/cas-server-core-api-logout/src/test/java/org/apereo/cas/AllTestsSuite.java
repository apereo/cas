package org.apereo.cas;

import org.apereo.cas.logout.LogoutExecutionPlanTests;
import org.apereo.cas.logout.LogoutPostProcessorTests;
import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandlerTests;

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
    LogoutExecutionPlanTests.class,
    LogoutPostProcessorTests.class,
    SingleLogoutServiceMessageHandlerTests.class
})
@Suite
public class AllTestsSuite {
}
