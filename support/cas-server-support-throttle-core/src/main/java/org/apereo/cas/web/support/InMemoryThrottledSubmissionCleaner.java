package org.apereo.cas.web.support;

import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * This is {@link InMemoryThrottledSubmissionCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class InMemoryThrottledSubmissionCleaner implements Runnable {
    private final AuthenticationThrottlingExecutionPlan authenticationThrottlingExecutionPlan;

    /**
     * Kicks off the job that attempts to clean the throttling submission record history.
     */
    @Override
    @Scheduled(initialDelayString = "${cas.authn.throttle.schedule.start-delay:PT10S}",
        fixedDelayString = "${cas.authn.throttle.schedule.repeat-interval:PT15S}")
    public void run() {
        val handlers = authenticationThrottlingExecutionPlan.getAuthenticationThrottleInterceptors();
        handlers
            .stream()
            .filter(handler -> handler instanceof ThrottledSubmissionHandlerInterceptor)
            .map(handler -> (ThrottledSubmissionHandlerInterceptor) handler)
            .forEach(ThrottledSubmissionHandlerInterceptor::decrement);
    }
}
