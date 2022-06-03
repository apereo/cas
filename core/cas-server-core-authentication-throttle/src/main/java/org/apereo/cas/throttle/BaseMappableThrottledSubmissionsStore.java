package org.apereo.cas.throttle;

import org.apereo.cas.web.support.ThrottledSubmission;
import org.apereo.cas.web.support.ThrottledSubmissionsStore;

import lombok.RequiredArgsConstructor;

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
public abstract class BaseMappableThrottledSubmissionsStore<T extends ThrottledSubmission>
    implements ThrottledSubmissionsStore<T> {
    /**
     * The backend map that tracks the submission attempts.
     */
    protected final Map<String, T> backingMap;

    @Override
    public void put(final T submission) {
        backingMap.put(submission.getKey(), submission);
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
    public void removeIf(final Predicate<T> condition) {
        backingMap.entrySet().removeIf(entry -> condition.test(entry.getValue()));
    }
}
