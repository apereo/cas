package org.apereo.cas.monitor;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.function.Supplier;

/**
 * This is {@link DefaultExecutableObserver}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class DefaultExecutableObserver implements ExecutableObserver {
    private static final KeyValue[] EMPTY_KEYVALUES_ARRAY = {};

    private final ObservationRegistry observationRegistry;

    @Override
    public void run(final MonitorableTask task, final Runnable runnable) {
        prepareObservation(task).observe(runnable);
    }

    @Override
    public <T> T supply(final MonitorableTask task, final Supplier<T> supplier) throws Throwable {
        return prepareObservation(task).observe(supplier);
    }

    protected Observation prepareObservation(final MonitorableTask task) {
        val highCardinalityValues = toKeyValueArray(task.getUnboundedValues());
        val lowCardinalityValues = toKeyValueArray(task.getBoundedValues());
        return Observation.createNotStarted(task.getName(), observationRegistry)
            .lowCardinalityKeyValues(KeyValues.of(lowCardinalityValues))
            .highCardinalityKeyValues(KeyValues.of(highCardinalityValues))
            .contextualName(task.getName());
    }

    private static KeyValue[] toKeyValueArray(final Map<String, String> values) {
        return values
            .entrySet()
            .stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
            .map(entry -> KeyValue.of(entry.getKey(), entry.getValue()))
            .toList()
            .toArray(EMPTY_KEYVALUES_ARRAY);
    }
}
