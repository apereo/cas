package org.apereo.cas.web.support;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditTrailExecutionPlan;

import javax.servlet.http.HttpServletRequest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
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

    public AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter(final int failureThreshold,
                                                                        final int failureRangeInSeconds,
                                                                        final String usernameParameter,
                                                                        final String authenticationFailureCode,
                                                                        final AuditTrailExecutionPlan auditTrailExecutionPlan,
                                                                        final String applicationCode,
                                                                        final ConcurrentMap map) {
        super(failureThreshold, failureRangeInSeconds, usernameParameter,
            authenticationFailureCode, auditTrailExecutionPlan, applicationCode);
        this.ipMap = map;
    }

    @Override
    public boolean exceedsThreshold(final HttpServletRequest request) {
        final ZonedDateTime last = this.ipMap.get(constructKey(request));
        return last != null && submissionRate(ZonedDateTime.now(ZoneOffset.UTC), last) > getThresholdRate();
    }

    @Override
    public void recordSubmissionFailure(final HttpServletRequest request) {
        this.ipMap.put(constructKey(request), ZonedDateTime.now(ZoneOffset.UTC));
    }

    /**
     * This class relies on an external configuration to clean it up.
     * It ignores the threshold data in the parent class.
     */
    @Override
    public void decrement() {
        LOGGER.info("Beginning audit cleanup...");

        final Set<Entry<String, ZonedDateTime>> keys = this.ipMap.entrySet();
        LOGGER.debug("Decrementing counts for throttler.  Starting key count: [{}]", keys.size());

        final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        for (final Iterator<Entry<String, ZonedDateTime>> iter = keys.iterator(); iter.hasNext();) {
            final Entry<String, ZonedDateTime> entry = iter.next();
            if (submissionRate(now, entry.getValue()) < getThresholdRate()) {
                LOGGER.trace("Removing entry for key [{}]", entry.getKey());
                iter.remove();
            }
        }
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
        return SUBMISSION_RATE_DIVIDEND / (a.toInstant().toEpochMilli() - b.toInstant().toEpochMilli());
    }
}
