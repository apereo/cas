package org.apereo.cas.web.support;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * This is {@link InMemoryThrottledSubmissionCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class InMemoryThrottledSubmissionCleaner {

    private HandlerInterceptor throttlingAdapter;

    public InMemoryThrottledSubmissionCleaner(
            final HandlerInterceptor throttlingAdapter) {
        this.throttlingAdapter = throttlingAdapter;
    }

    /**
     * Kicks off the job that attempts to clean the throttling submission record history.
     */
    @Scheduled(initialDelayString = "${cas.authn.throttle.startDelay:10000}",
            fixedDelayString = "${cas.authn.throttle.repeatInterval:300000}")
    public void clean() {
        if (throttlingAdapter instanceof AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter) {
            AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter.class
                    .cast(throttlingAdapter).decrementCounts();
        }
    }
}
