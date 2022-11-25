package org.apereo.cas.throttle;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.ThrottledSubmission;
import org.apereo.cas.web.support.ThrottledSubmissionsStore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
        LOGGER.debug("Submitting rate for [{}] and [{}] is [{}]", a, b, rate);
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
        LOGGER.debug("Last throttling date time for key [{}] is [{}]", key, submissionEntry);
        return submissionEntry != null && submissionRate(ZonedDateTime.now(ZoneOffset.UTC), submissionEntry.getValue()) > thresholdRate;
    }

    @Override
    public void release(final double thresholdRate) {
        val now = ZonedDateTime.now(ZoneOffset.UTC);
        removeIf(entry -> submissionRate(now, entry.getValue()) < thresholdRate);
    }
}
