package org.apereo.cas.monitor;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link MonitorableTask}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@With
@Getter
@RequiredArgsConstructor
public class MonitorableTask {
    /**
     * Bounded values are those that are expected to have a limited set of values,
     * such as a fixed set of status codes or predefined categories.
     * These values are typically used for low-cardinality metrics.
     * Examples include HTTP status codes, user roles, or predefined error types.
     */
    private final Map<String, String> boundedValues = new HashMap<>();

    /**
     * Unbounded values are those that can take on a wide range of values,
     * such as timestamps, unique identifiers, or any other data that does not have a fixed
     * set of possible values. These values are typically used for high-cardinality metrics.
     * Examples include user IDs, session IDs, or any other data that can vary widely.
     * Unbounded values are often used to provide detailed context for a metric,
     * allowing for more granular analysis and troubleshooting.
     */
    private final Map<String, String> unboundedValues = new HashMap<>();

    private final String name;

    /**
     * Collect bounded value.
     *
     * @param name  the name
     * @param value the value
     * @return the monitorable task
     */
    @CanIgnoreReturnValue
    public MonitorableTask withBoundedValue(final String name, final String value) {
        boundedValues.put(name, value);
        return this;
    }
}
