package org.apereo.cas.web.support;

import javax.servlet.http.HttpServletRequest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation of a HandlerInterceptorAdapter that keeps track of a mapping
 * of IP Addresses to number of failures to authenticate.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public abstract class AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter
        extends AbstractThrottledSubmissionHandlerInterceptorAdapter {

    private static final double SUBMISSION_RATE_DIVIDEND = 1000.0;

    private ConcurrentMap<String, ZonedDateTime> ipMap = new ConcurrentHashMap<>();

    @Override
    protected boolean exceedsThreshold(final HttpServletRequest request) {
        final ZonedDateTime last = this.ipMap.get(constructKey(request));
        return last != null && submissionRate(ZonedDateTime.now(ZoneOffset.UTC), last) > getThresholdRate();
    }

    @Override
    protected void recordSubmissionFailure(final HttpServletRequest request) {
        this.ipMap.put(constructKey(request), ZonedDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Construct key to be used by the throttling agent to track requests.
     *
     * @param request the request
     * @return the string
     */
    protected abstract String constructKey(HttpServletRequest request);

    /**
     * This class relies on an external configuration to clean it up.
     * It ignores the threshold data in the parent class.
     */
    public void decrementCounts() {

        logger.info("Beginning audit cleanup...");

        final Set<Entry<String, ZonedDateTime>> keys = this.ipMap.entrySet();
        logger.debug("Decrementing counts for throttler.  Starting key count: {}", keys.size());

        final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        for (final Iterator<Entry<String, ZonedDateTime>> iter = keys.iterator(); iter.hasNext(); ) {
            final Entry<String, ZonedDateTime> entry = iter.next();
            if (submissionRate(now, entry.getValue()) < getThresholdRate()) {
                logger.trace("Removing entry for key {}", entry.getKey());
                iter.remove();
            }
        }
        logger.debug("Done decrementing count for throttler.");
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
}
