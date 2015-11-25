package org.jasig.cas.web.support;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Base class for in-memory throttled submission handlers.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public abstract class AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapterTests
extends AbstractThrottledSubmissionHandlerInterceptorAdapterTests {

    @Override
    protected MockHttpServletResponse loginUnsuccessfully(final String username, final String fromAddress) throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("POST");
        request.setParameter("username", username);
        request.setRemoteAddr(fromAddress);
        final MockRequestContext context = new MockRequestContext();
        context.setCurrentEvent(new Event("", "error"));
        request.setAttribute("flowRequestContext", context);
        getThrottle().preHandle(request, response, null);
        getThrottle().postHandle(request, response, null, null);
        return response;
    }
}
