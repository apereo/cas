package org.apereo.cas.web.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

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
    @Qualifier("inMemoryIpAddressThrottle")
    private AsyncHandlerInterceptor throttle;

    @Override
    protected AsyncHandlerInterceptor getThrottle() {
        return throttle;
    }
}
