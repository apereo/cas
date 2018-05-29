package org.apereo.cas.web.support;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DefaultAuthenticationThrottlingExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Slf4j
public class DefaultAuthenticationThrottlingExecutionPlan implements AuthenticationThrottlingExecutionPlan {
    private List<HandlerInterceptor> authenticationThrottleInterceptors = new ArrayList<>();

    @Override
    public void registerAuthenticationThrottleInterceptor(final HandlerInterceptor handler) {
        this.authenticationThrottleInterceptors.add(handler);
    }
}
