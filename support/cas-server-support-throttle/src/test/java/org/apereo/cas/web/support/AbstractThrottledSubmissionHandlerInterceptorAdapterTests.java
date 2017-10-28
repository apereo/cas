package org.apereo.cas.web.support;

import org.apache.http.HttpStatus;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.web.support.config.CasThrottlingConfiguration;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Base class for submission throttle tests.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class, 
        CasCoreUtilConfiguration.class,
        AopAutoConfiguration.class, 
        CasThrottlingConfiguration.class})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@TestPropertySource(properties = "spring.aop.proxy-target-class=true")
@EnableScheduling
public abstract class AbstractThrottledSubmissionHandlerInterceptorAdapterTests {
    protected static final String IP_ADDRESS = "1.2.3.4";
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractThrottledSubmissionHandlerInterceptorAdapterTests.class);
    
    @Autowired
    @Qualifier("authenticationThrottle")
    protected ThrottledSubmissionHandlerInterceptor throttle;
    
    @Before
    public void setUp() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(IP_ADDRESS);
        request.setLocalAddr(IP_ADDRESS);
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
    }

    @After
    public void tearDown() {
        ClientInfoHolder.setClientInfo(null);
    }

    @Test
    public void verifyThrottle() throws Exception {
        // Ensure that repeated logins BELOW threshold rate are allowed
        failLoop(3, 1000, HttpStatus.SC_UNAUTHORIZED);

        // Ensure that repeated logins ABOVE threshold rate are throttled
        failLoop(3, 200, HttpStatus.SC_LOCKED);

        // Ensure that slowing down relieves throttle
        throttle.decrement();
        Thread.sleep(1000);
        failLoop(3, 1000, HttpStatus.SC_UNAUTHORIZED);
    }


    private void failLoop(final int trials, final int period, final int expected) throws Exception {
        // Seed with something to compare against
        loginUnsuccessfully("mog", "1.2.3.4");

        for (int i = 0; i < trials; i++) {
            LOGGER.debug("Waiting for [{}] ms", period);
            Thread.sleep(period);
            
            final MockHttpServletResponse status = loginUnsuccessfully("mog", "1.2.3.4");
            assertEquals(expected, status.getStatus());
        }
    }


    protected abstract MockHttpServletResponse loginUnsuccessfully(String username, String fromAddress) throws Exception;
    
}
