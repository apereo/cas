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
    private final Map<String, String> boundedValues = new HashMap<>();

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
