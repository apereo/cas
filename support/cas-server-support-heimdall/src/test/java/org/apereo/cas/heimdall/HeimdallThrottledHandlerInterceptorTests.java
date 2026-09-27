package org.apereo.cas.heimdall;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link HeimdallThrottledHandlerInterceptorTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Authorization")
class HeimdallThrottledHandlerInterceptorTests {

    @ParameterizedTest
    @CsvSource({"200,false", "400,false", "403,false", "404,false", "500,false", "401,true"})
    void verifyOnlyFailedAuthenticationIsRecorded(final int status, final boolean recorded) throws Exception {
        val delegate = mock(HandlerInterceptor.class);
        when(delegate.preHandle(any(), any(), any())).thenReturn(true);
        val interceptor = new HeimdallThrottledHandlerInterceptor(delegate);
        val request = new MockHttpServletRequest("POST", "/cas/heimdall/authzen");
        val response = new MockHttpServletResponse();
        val handler = new Object();

        assertTrue(interceptor.preHandle(request, response, handler));
        verify(delegate).preHandle(request, response, handler);

        response.setStatus(status);
        interceptor.postHandle(request, response, handler, null);
        interceptor.afterCompletion(request, response, handler, null);
        verify(delegate, times(recorded ? 1 : 0)).postHandle(request, response, handler, null);
        verify(delegate, times(recorded ? 1 : 0)).afterCompletion(request, response, handler, null);
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void verifyThrottledCallerIsRefused(final boolean allowed) throws Exception {
        val delegate = mock(HandlerInterceptor.class);
        when(delegate.preHandle(any(), any(), any())).thenReturn(allowed);
        val interceptor = new HeimdallThrottledHandlerInterceptor(delegate);
        assertEquals(allowed, interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object()));
    }
}
