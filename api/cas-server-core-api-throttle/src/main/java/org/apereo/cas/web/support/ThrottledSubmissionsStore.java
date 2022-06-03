package org.apereo.cas.web.support;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This is {@link ThrottledSubmissionsStore}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public interface ThrottledSubmissionsStore<T extends ThrottledSubmission> {
    /**
     *Default bean name.
     */
    String BEAN_NAME = "throttleSubmissionMap";
    /**
     * Remove element passing the given condition.
     *
     * @param condition the condition
     */
    void removeIf(Predicate<T> condition);

    /**
     * Put.
     *
     * @param submission the submission
     */
    void put(T submission);

    /**
     * Get zoned date time.
     *
     * @param key the key
     * @return the zoned date time
     */
    T get(String key);

    /**
     * Get entries.
     *
     * @return the stream
     */
    Stream<T> entries();
}
