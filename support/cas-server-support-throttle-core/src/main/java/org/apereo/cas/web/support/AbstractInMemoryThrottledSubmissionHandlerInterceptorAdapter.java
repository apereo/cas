package org.apereo.cas.web.support;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.servlet.http.HttpServletRequest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation of a HandlerInterceptorAdapter that keeps track of a mapping
 * of IP Addresses to number of failures to authenticate.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
public abstract class AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter extends AbstractThrottledSubmissionHandlerInterceptorAdapter
    implements InMemoryThrottledSubmissionHandlerInterceptor {

    private static final double SUBMISSION_RATE_DIVIDEND = 1000.0;

    private final ConcurrentMap<String, ZonedDateTime> ipMap;

    public AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter(
        final ThrottledSubmissionHandlerConfigurationContext configurationContext,
        final ConcurrentMap<String, ZonedDateTime> ipMap) {
        super(configurationContext);
        this.ipMap = ipMap;
    }

    /**
     * Computes the instantaneous rate in between two given dates corresponding to two submissions.
     *
     * @param a First date.
     * @param b Second date.
     * @return Instantaneous submission rate in submissions/sec, e.g. {@code a - b}.
     */
    private static double submissionRate(final ZonedDateTime a, final ZonedDateTime b) {
        return SUBMISSION_RATE_DIVIDEND / (a.toInstant().toEpochMilli() - b.toInstant().toEpochMilli());
    }

    @Override
    public boolean exceedsThreshold(final HttpServletRequest request) {
        val last = this.ipMap.get(constructKey(request));
        return last != null && submissionRate(ZonedDateTime.now(ZoneOffset.UTC), last) > getThresholdRate();
    }

    @Override
    public void recordSubmissionFailure(final HttpServletRequest request) {
        val key = constructKey(request);
        LOGGER.debug("Recording submission failure [{}]", key);
        this.ipMap.put(key, ZonedDateTime.now(ZoneOffset.UTC));
    }

    /**
     * This class relies on an external configuration to clean it up.
     * It ignores the threshold data in the parent class.
     */
    @Override
    public void decrement() {
        LOGGER.info("Beginning audit cleanup...");
        val now = ZonedDateTime.now(ZoneOffset.UTC);
        this.ipMap.entrySet().removeIf(entry -> submissionRate(now, entry.getValue()) < getThresholdRate());
        LOGGER.debug("Done decrementing count for throttler.");
    }
}
