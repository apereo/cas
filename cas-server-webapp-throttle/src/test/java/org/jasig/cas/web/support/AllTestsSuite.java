package org.jasig.cas.web.support;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests.class,
        InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests.class,
        InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests.class
})
public class AllTestsSuite {
}
