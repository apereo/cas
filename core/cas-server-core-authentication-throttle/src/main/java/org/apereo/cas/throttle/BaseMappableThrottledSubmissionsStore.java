package org.apereo.cas.throttle;

import org.apereo.cas.web.support.ThrottledSubmission;
import org.apereo.cas.web.support.ThrottledSubmissionsStore;

import lombok.RequiredArgsConstructor;

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
public abstract class BaseMappableThrottledSubmissionsStore implements ThrottledSubmissionsStore {
    /**
     * The backend map that tracks the submission attempts.
     */
    protected final Map<String, ZonedDateTime> backingMap;

    @Override
    public void put(final String key, final ZonedDateTime value) {
        backingMap.put(key, value);
    }

    @Override
    public ZonedDateTime get(final String key) {
        return backingMap.get(key);
    }

    @Override
    public Stream<ThrottledSubmission> entries() {
        return backingMap.entrySet().stream()
            .map(entry -> new ThrottledSubmission(entry.getKey(), entry.getValue()));
    }

    @Override
    public void removeIf(final Predicate<ThrottledSubmission> condition) {
        backingMap.entrySet()
            .removeIf(entry -> condition.test(new ThrottledSubmission(entry.getKey(), entry.getValue())));
    }
}
