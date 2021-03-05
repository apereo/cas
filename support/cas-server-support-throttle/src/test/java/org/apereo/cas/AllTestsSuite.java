package org.apereo.cas;

import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapterTests;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerEndpointTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests.class,
    InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapterTests.class,
    ThrottledSubmissionHandlerEndpointTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
