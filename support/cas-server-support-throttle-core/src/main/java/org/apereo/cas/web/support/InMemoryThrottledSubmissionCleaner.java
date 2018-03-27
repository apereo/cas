package org.apereo.cas.web.support;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * This is {@link InMemoryThrottledSubmissionCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@AllArgsConstructor
public class InMemoryThrottledSubmissionCleaner implements Runnable {
    private final ThrottledSubmissionHandlerInterceptor throttlingAdapter;

    /**
     * Kicks off the job that attempts to clean the throttling submission record history.
     */
    @Override
    @Scheduled(initialDelayString = "${cas.authn.throttle.schedule.startDelay:PT10S}",
               fixedDelayString = "${cas.authn.throttle.schedule.repeatInterval:PT15S}")
    public void run() {
        this.throttlingAdapter.decrement();
    }
}
