package org.apereo.cas.web.support;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ThrottledSubmissionHandlerInterceptorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("AuthenticationThrottling")
public class ThrottledSubmissionHandlerInterceptorTests {

    @Test
    public void verifyOperation() throws Exception {
        val input = new ThrottledSubmissionHandlerInterceptor() {
        };
        assertNotNull(input.getName());
        assertFalse(input.exceedsThreshold(new MockHttpServletRequest()));
        assertTrue(input.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object()));

        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                input.decrement();
                input.recordSubmissionFailure(new MockHttpServletRequest());
                input.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), new ModelAndView());
                input.afterConcurrentHandlingStarted(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object());
                input.afterCompletion(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), new RuntimeException());
            }
        });
    }
}
