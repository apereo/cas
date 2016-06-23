package org.apereo.cas.web.support;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Unit test for {@link InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter}.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapterTests
extends AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapterTests {

    @Autowired
    private InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter throttle;

    @Override
    protected AbstractThrottledSubmissionHandlerInterceptorAdapter getThrottle() {
        return throttle;
    }
}
