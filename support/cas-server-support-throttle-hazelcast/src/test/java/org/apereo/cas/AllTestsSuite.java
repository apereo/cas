package org.apereo.cas;

import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests;

import lombok.extern.slf4j.Slf4j;
import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests.class,
    InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests.class
})
@Slf4j
public class AllTestsSuite {
}
