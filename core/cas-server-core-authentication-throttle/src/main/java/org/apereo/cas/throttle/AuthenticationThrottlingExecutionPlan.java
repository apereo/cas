package org.apereo.cas.throttle;

import module java.base;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * This is {@link AuthenticationThrottlingExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface AuthenticationThrottlingExecutionPlan {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "authenticationThrottlingExecutionPlan";

    /**
     * Register authentication throttle interceptor.
     *
     * @param handler the handler
     * @return the authentication throttling execution plan
     */
    AuthenticationThrottlingExecutionPlan registerAuthenticationThrottleInterceptor(HandlerInterceptor handler);

    /**
     * Register authentication throttle filter.
     *
     * @param filter the filter
     * @return the authentication throttling execution plan
     */
    AuthenticationThrottlingExecutionPlan registerAuthenticationThrottleFilter(ThrottledRequestFilter filter);

    /**
     * Gets authentication throttle interceptor.
     *
     * @return the authentication throttle interceptor
     */
    List<HandlerInterceptor> getAuthenticationThrottleInterceptors();

    /**
     * Gets authentication throttle filter.
     *
     * @return the authentication throttle filter
     */
    ThrottledRequestFilter getAuthenticationThrottleFilter();
}
