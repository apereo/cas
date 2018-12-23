package org.apereo.cas.throttle;

import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * This is {@link AuthenticationThrottlingExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface AuthenticationThrottlingExecutionPlan {

    /**
     * Register authentication throttle interceptor.
     *
     * @param handler the handler
     */
    void registerAuthenticationThrottleInterceptor(HandlerInterceptor handler);

    /**
     * Gets authentication throttle interceptor.
     *
     * @return the authentication throttle interceptor
     */
    List<HandlerInterceptor> getAuthenticationThrottleInterceptors();
}
