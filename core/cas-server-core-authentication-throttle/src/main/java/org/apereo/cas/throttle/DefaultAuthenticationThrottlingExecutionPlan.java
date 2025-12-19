package org.apereo.cas.throttle;

import module java.base;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import org.springframework.web.servlet.HandlerInterceptor;

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
    @CanIgnoreReturnValue
    public AuthenticationThrottlingExecutionPlan registerAuthenticationThrottleInterceptor(final HandlerInterceptor handler) {
        this.authenticationThrottleInterceptors.add(handler);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
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
