package org.apereo.cas.throttle;

import lombok.Getter;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DefaultAuthenticationThrottlingExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DefaultAuthenticationThrottlingExecutionPlan implements AuthenticationThrottlingExecutionPlan {
    @Getter
    private final List<HandlerInterceptor> authenticationThrottleInterceptors = new ArrayList<>();

    private final List<ThrottledRequestFilter> authenticationThrottleFilters = new ArrayList<>();

    @Override
    public AuthenticationThrottlingExecutionPlan registerAuthenticationThrottleInterceptor(final HandlerInterceptor handler) {
        this.authenticationThrottleInterceptors.add(handler);
        return this;
    }

    @Override
    public AuthenticationThrottlingExecutionPlan registerAuthenticationThrottleFilter(final ThrottledRequestFilter filter) {
        this.authenticationThrottleFilters.add(filter);
        return this;
    }

    @Override
    public ThrottledRequestFilter getAuthenticationThrottleFilter() {
        return (request, response) -> authenticationThrottleFilters
            .stream()
            .anyMatch(filter -> filter.supports(request, response));
    }
}
