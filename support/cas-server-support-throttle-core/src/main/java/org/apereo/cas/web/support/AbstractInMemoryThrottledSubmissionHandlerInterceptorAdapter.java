package org.apereo.cas.web.support;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.servlet.http.HttpServletRequest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Implementation of a {@link InMemoryThrottledSubmissionHandlerInterceptor} that keeps track of a mapping
 * of IP Addresses to number of failures to authenticate.
 * This class relies on an external configuration to clean it up.
 * It ignores the threshold data in the parent class.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
public abstract class AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter extends AbstractThrottledSubmissionHandlerInterceptorAdapter
    implements InMemoryThrottledSubmissionHandlerInterceptor {

    private static final double SUBMISSION_RATE_DIVIDEND = 1000.0;

    private final ConcurrentMap<String, ZonedDateTime> ipMap;

    protected AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter(
        final ThrottledSubmissionHandlerConfigurationContext configurationContext,
        final ConcurrentMap<String, ZonedDateTime> ipMap) {
        super(configurationContext);
        this.ipMap = ipMap;
    }

    @Override
    public void recordSubmissionFailure(final HttpServletRequest request) {
        val key = constructKey(request);
        LOGGER.debug("Recording submission failure [{}]", key);
        this.ipMap.put(key, ZonedDateTime.now(ZoneOffset.UTC));
    }

    @Override
    public boolean exceedsThreshold(final HttpServletRequest request) {
        val key = constructKey(request);
        LOGGER.trace("Throttling threshold key is [{}] with submission threshold [{}]", key, getThresholdRate());
        val last = this.ipMap.get(key);
        LOGGER.debug("Last throttling date time for key [{}] is [{}]", key, last);
        return last != null && submissionRate(ZonedDateTime.now(ZoneOffset.UTC), last) > getThresholdRate();
    }

    @Override
    public Collection getRecords() {
        return ipMap.entrySet()
            .stream()
            .map(entry -> entry.getKey() + "<->" + entry.getValue())
            .collect(Collectors.toList());
    }

    @Override
    public void decrement() {
        LOGGER.info("Beginning audit cleanup...");
        val now = ZonedDateTime.now(ZoneOffset.UTC);
        this.ipMap.entrySet().removeIf(entry -> submissionRate(now, entry.getValue()) < getThresholdRate());
        LOGGER.debug("Done decrementing count for throttler.");
    }

    /**
     * Computes the instantaneous rate in between two given dates corresponding to two submissions.
     *
     * @param a First date.
     * @param b Second date.
     * @return Instantaneous submission rate in submissions/sec, e.g. {@code a - b}.
     */
    private static double submissionRate(final ZonedDateTime a, final ZonedDateTime b) {
        val rate = SUBMISSION_RATE_DIVIDEND / (a.toInstant().toEpochMilli() - b.toInstant().toEpochMilli());
        LOGGER.debug("Submitting rate for [{}] and [{}] is [{}]", a, b, rate);
        return rate;
    }
}
