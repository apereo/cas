package org.apereo.cas.web.support;

import org.springframework.scheduling.annotation.Scheduled;

/**
 * This is {@link InMemoryThrottledSubmissionCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class InMemoryThrottledSubmissionCleaner implements Runnable {

    private final ThrottledSubmissionHandlerInterceptor throttlingAdapter;

    public InMemoryThrottledSubmissionCleaner(final ThrottledSubmissionHandlerInterceptor throttlingAdapter) {
        this.throttlingAdapter = throttlingAdapter;
    }

    /**
     * Kicks off the job that attempts to clean the throttling submission record history.
     */
    @Override
    @Scheduled(initialDelayString = "${cas.authn.throttle.startDelay:10000}",
            fixedDelayString = "${cas.authn.throttle.repeatInterval:300000}")
    public void run() {
        this.throttlingAdapter.decrement();
    }
}
