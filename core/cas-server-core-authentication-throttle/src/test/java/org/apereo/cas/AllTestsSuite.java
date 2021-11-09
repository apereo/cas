package org.apereo.cas;

import org.apereo.cas.throttle.DefaultAuthenticationThrottlingExecutionPlanTests;
import org.apereo.cas.throttle.DefaultThrottledRequestResponseHandlerTests;
import org.apereo.cas.throttle.ThrottledRequestFilterTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    DefaultAuthenticationThrottlingExecutionPlanTests.class,
    ThrottledRequestFilterTests.class,
    DefaultThrottledRequestResponseHandlerTests.class
})
@Suite
public class AllTestsSuite {
}
