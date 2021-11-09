package org.apereo.cas.throttle;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultAuthenticationThrottlingExecutionPlanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("AuthenticationThrottling")
public class DefaultAuthenticationThrottlingExecutionPlanTests {

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val plan = new DefaultAuthenticationThrottlingExecutionPlan();
        plan.registerAuthenticationThrottleFilter(ThrottledRequestFilter.httpPost());
        assertFalse(plan.getAuthenticationThrottleFilter().supports(request, response));

        request.setMethod(HttpMethod.POST.name());
        assertTrue(plan.getAuthenticationThrottleFilter().supports(request, response));
    }
}
