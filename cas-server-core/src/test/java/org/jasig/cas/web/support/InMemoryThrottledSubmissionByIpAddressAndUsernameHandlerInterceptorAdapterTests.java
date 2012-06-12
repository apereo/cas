/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.web.support;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.scheduling.quartz.SimpleTriggerBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * Unit test for {@link InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests}.
 *
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/throttledSubmissionContext.xml"})
public class InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests {

    private static final int FAILURE_RANGE = 5;

    private static final int FAILURE_THRESHOLD = 100;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter throttle;

    @Autowired
    @Qualifier("memoryThrottleIpAndUsernameTrigger")
    private SimpleTriggerBean trigger;


    @Before
    public void setUp() throws Exception {
        throttle.setFailureRangeInSeconds(FAILURE_RANGE);
        throttle.setFailureThreshold(FAILURE_THRESHOLD);
        throttle.afterPropertiesSet();
    }


    @Test
    public void testThrottle() throws Exception {
        final double rate = (double) FAILURE_THRESHOLD / (double) FAILURE_RANGE;

        // Ensure that repeated logins BELOW threshold rate are allowed
        assertEquals(200, loginUnsuccessfully("mog", "1.2.3.4").getStatus());
        int wait;
        for (int i = 0; i < 5; i++) {
            // Wait 5% more than threshold period (rate slightly below threshold)
            wait = (int)(1000.0 * 1.05 / rate);
            logger.debug("Waiting for {} ms", wait);
            Thread.sleep(wait);
            assertEquals(200, loginUnsuccessfully("mog", "1.2.3.4").getStatus());
        }

        // Ensure that repeated logins ABOVE threshold are throttled
        // The following attempt follows immediately after last one in above loop, which is effectively
        // instantaneous, and is expected to be throttled.
        assertEquals(403, loginUnsuccessfully("mog", "1.2.3.4").getStatus());
        for (int i = 1; i < 5; i++) {
            // Wait 5% less than threshold period (rate slightly above threshold)
            wait = (int)(1000.0 * 0.95 / rate);
            logger.debug("Waiting for {} ms", wait);
            Thread.sleep(wait);
            assertEquals(403, loginUnsuccessfully("mog", "1.2.3.4").getStatus());
        }

        // Ensure that slowing down relieves throttle
        // Wait 5% more than threshold period (rate slightly below threshold)
        wait = (int)(1000.0 * 1.05 / rate);
        Thread.sleep(wait);
        assertEquals(200, loginUnsuccessfully("mog", "1.2.3.4").getStatus());
    }


    private MockHttpServletResponse loginUnsuccessfully(final String username, final String fromAddress) throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("POST");
        request.setParameter("username", username);
        request.setRemoteAddr(fromAddress);
        MockRequestContext context = new MockRequestContext();
        context.setCurrentEvent(new Event("", "error"));
        request.setAttribute("flowRequestContext", context);
        throttle.preHandle(request, response, null);
        throttle.postHandle(request, response, null, null);
        return response;
    }
}
