package org.apereo.cas;

import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapterTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests.class,
    InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapterTests.class
})
public class AllTestsSuite {
}
