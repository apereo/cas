package org.jasig.cas.web.support;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit test for {@link InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter}.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/inMemoryThrottledSubmissionContext.xml"})
public class InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests
extends AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapterTests {

    @Autowired
    private InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter throttle;

    @Override
    protected AbstractThrottledSubmissionHandlerInterceptorAdapter getThrottle() {
        return throttle;
    }
}
