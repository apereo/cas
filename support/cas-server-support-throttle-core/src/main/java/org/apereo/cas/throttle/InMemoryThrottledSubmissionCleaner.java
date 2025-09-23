package org.apereo.cas.throttle;

import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;

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

    @Override
    @Scheduled(
        cron = "${cas.authn.throttle.schedule.cron-expression:}",
        zone = "${cas.authn.throttle.schedule.cron-time-zone:}",
        initialDelayString = "${cas.authn.throttle.schedule.start-delay:PT10S}",
        fixedDelayString = "${cas.authn.throttle.schedule.repeat-interval:PT15S}")
    public void run() {
        val handlers = authenticationThrottlingExecutionPlan.getAuthenticationThrottleInterceptors();
        handlers
            .stream()
            .filter(ThrottledSubmissionHandlerInterceptor.class::isInstance)
            .map(ThrottledSubmissionHandlerInterceptor.class::cast)
            .forEach(ThrottledSubmissionHandlerInterceptor::release);
    }
}
