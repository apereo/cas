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
     * Default bean name.
     */
    String BEAN_NAME = "throttleSubmissionStore";

    /**
     * Remove element passing the given condition.
     *
     * @param condition the condition
     */
    void removeIf(Predicate<T> condition);

    /**
     * Remove.
     *
     * @param key the key
     */
    void remove(String key);

    /**
     * Put.
     *
     * @param submission the submission
     */
    void put(T submission);

    /**
     * Contains this key?
     *
     * @param key the key
     * @return true/false
     */
    boolean contains(String key);

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

    /**
     * Exceeds threshold?
     *
     * @param key           the key
     * @param thresholdRate the threshold rate
     * @return true/false
     */
    boolean exceedsThreshold(String key, double thresholdRate);

    /**
     * Release.
     *
     * @param thresholdRate the threshold rate
     */
    void release(double thresholdRate);

    /**
     * Clear all store records.
     */
    void clear();
}
