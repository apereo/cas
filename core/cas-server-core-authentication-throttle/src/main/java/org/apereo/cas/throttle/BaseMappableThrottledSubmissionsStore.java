package org.apereo.cas.throttle;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.ThrottledSubmission;
import org.apereo.cas.web.support.ThrottledSubmissionsStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This is {@link BaseMappableThrottledSubmissionsStore}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Slf4j
public abstract class BaseMappableThrottledSubmissionsStore<T extends ThrottledSubmission>
    implements ThrottledSubmissionsStore<T> {

    private static final double SUBMISSION_RATE_DIVIDEND = 1000.0;

    /**
     * The backend map that tracks the submission attempts.
     */
    protected final Map<String, T> backingMap;

    /**
     * CAS configuration properties.
     */
    protected final CasConfigurationProperties casProperties;

    /**
     * Computes the instantaneous rate in between two given dates corresponding to two submissions.
     *
     * @param a First date.
     * @param b Second date.
     * @return Instantaneous submission rate in submissions/sec, e.g. {@code a - b}.
     */
    private static double submissionRate(final ZonedDateTime a, final ZonedDateTime b) {
        val rate = SUBMISSION_RATE_DIVIDEND / (a.toInstant().toEpochMilli() - b.toInstant().toEpochMilli());
        LOGGER.debug("Submission rate between [{}] and [{}] is [{}]", a, b, rate);
        return rate;
    }

    @Override
    public void removeIf(final Predicate<T> condition) {
        backingMap.entrySet().removeIf(entry -> condition.test(entry.getValue()));
    }

    @Override
    public void remove(final String key) {
        backingMap.remove(key);
    }

    @Override
    public void put(final T submission) {
        backingMap.put(submission.getKey(), submission);
    }

    @Override
    public boolean contains(final String key) {
        return backingMap.containsKey(key);
    }

    @Override
    public T get(final String key) {
        return backingMap.get(key);
    }

    @Override
    public Stream<T> entries() {
        return backingMap.values().stream();
    }

    @Override
    public boolean exceedsThreshold(final String key, final double thresholdRate) {
        val submissionEntry = get(key);
        LOGGER.debug("Last throttling date for key [{}] is [{}]", key, submissionEntry);
        if (submissionEntry != null) {
            val now = ZonedDateTime.now(ZoneOffset.UTC);
            val submissionRate = submissionRate(now, submissionEntry.getValue());
            val result = submissionRate > thresholdRate;
            LOGGER.debug("Current time is [{}] and submission date is [{}]. Submission rate between the dates is [{}] and your threshold rate [{}]. "
                    + "The submission rate is [{}] than the threshold rate, so the request [{}] be throttled.",
                now, submissionEntry.getValue(), submissionRate, thresholdRate,
                BooleanUtils.toString(result, "greater", "less"),
                BooleanUtils.toString(result, "may", "may not"));
            return result;
        }
        return false;
    }

    @Override
    public void clear() {
        backingMap.clear();
    }

    @Override
    public void release(final double thresholdRate) {
        val now = ZonedDateTime.now(ZoneOffset.UTC);
        LOGGER.debug("Attempting to release throttled records for now [{}]", now);
        removeIf(entry -> {
            if (entry.hasExpiredAlready()) {
                LOGGER.debug("Throttled submission [{}] has expired and will be removed", entry.getKey());
                return true;
            }
            if (entry.isStillInExpirationWindow()) {
                LOGGER.debug("Throttled submission [{}] has not expired and can only be released at [{}]",
                    entry.getKey(), entry.getExpiration());
                return false;
            }
            val submissionRate = submissionRate(now, entry.getValue());
            val result = submissionRate < thresholdRate;
            LOGGER.trace("Your threshold rate is [{}]. Submission rate between now [{}] and throttled entry [{}] @ [{}] is [{}]. "
                    + "This is [{}] than the threshold rate so the submission [{}] be released and removed from the store.",
                thresholdRate, now, entry.getId(), entry.getValue(), submissionRate,
                BooleanUtils.toString(result, "less", "greater"),
                BooleanUtils.toString(result, "may", "may not"));
            return result;
        });
    }
}
