package org.apereo.cas.monitor;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.fi.util.function.CheckedSupplier;
import org.springframework.beans.factory.ObjectProvider;
import java.util.Map;

/**
 * This is {@link DefaultExecutableObserver}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class DefaultExecutableObserver implements ExecutableObserver {
    private static final KeyValue[] EMPTY_KEY_VALUES_ARRAY = {};

    private final ObjectProvider<ObservationRegistry> observationRegistry;

    @Override
    public void run(final MonitorableTask task, final Runnable runnable) {
        prepareObservation(task).observe(runnable);
    }

    @Override
    public <T> T supply(final MonitorableTask task, final CheckedSupplier<T> supplier) {
        return prepareObservation(task).observe(CheckedSupplier.sneaky(supplier));
    }

    protected Observation prepareObservation(final MonitorableTask task) {
        val highCardinalityValues = toKeyValueArray(task.getUnboundedValues());
        val lowCardinalityValues = toKeyValueArray(task.getBoundedValues());
        return Observation.createNotStarted(task.getName(), observationRegistry.getObject())
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
            .toArray(EMPTY_KEY_VALUES_ARRAY);
    }
}
