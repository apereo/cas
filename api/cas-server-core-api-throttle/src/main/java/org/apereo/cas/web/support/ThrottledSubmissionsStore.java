package org.apereo.cas.web.support;

import java.time.ZonedDateTime;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This is {@link ThrottledSubmissionsStore}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public interface ThrottledSubmissionsStore {
    /**
     * Remove element passing the given condition.
     *
     * @param condition the condition
     */
    void removeIf(Predicate<ThrottledSubmission> condition);

    /**
     * Put.
     *
     * @param key   the key
     * @param value the value
     */
    void put(String key, ZonedDateTime value);

    /**
     * Get zoned date time.
     *
     * @param key the key
     * @return the zoned date time
     */
    ZonedDateTime get(String key);

    /**
     * Get entries.
     *
     * @return the stream
     */
    Stream<ThrottledSubmission> entries();
}
