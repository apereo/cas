package org.apereo.cas.monitor;

import org.apereo.cas.util.function.FunctionUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Abstract base class for monitors that observe cache storage systems.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class AbstractCacheHealthIndicator extends AbstractHealthIndicator {

    private final long evictionThreshold;

    private final long threshold;

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        FunctionUtils.doAndHandle(bldr -> {
            val statistics = getStatistics();
            bldr.withDetail("name", getName());
            if (statistics == null || statistics.length == 0) {
                bldr.outOfService().withDetail("message", "Cache statistics are not available.");
                return;
            }

            val statuses = Arrays.stream(statistics)
                .map(AbstractCacheHealthIndicator.this::status)
                .collect(Collectors.toSet());

            if (statuses.contains(Status.OUT_OF_SERVICE)) {
                bldr.outOfService();
            } else if (statuses.contains(Status.DOWN)) {
                bldr.down();
            } else if (statuses.contains(new Status("WARN"))) {
                bldr.status("WARN");
            } else {
                bldr.up();
            }

            Arrays.stream(statistics).forEach(s -> {
                val map = new HashMap<String, Object>();
                map.put("size", s.getSize());
                map.put("capacity", s.getCapacity());
                map.put("evictions", s.getEvictions());
                map.put("percentFree", s.getPercentFree());
                map.put("state", s.toString(new StringBuilder()));

                bldr.withDetail(s.getName(), map);
            });
        }, throwable -> {
            builder.down(throwable);
            return builder;
        }).accept(builder);
    }

    /**
     * Gets name of this indicator.
     *
     * @return the name
     */
    protected String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Gets the statistics from this monitor.
     *
     * @return the statistics
     */
    protected abstract CacheStatistics[] getStatistics();

    /**
     * Computes the status code for a given set of cache statistics.
     *
     * @param statistics Cache statistics.
     * @return WARN or OUT_OF_SERVICE OR UP.
     */
    protected Status status(final CacheStatistics statistics) {
        if (statistics.getEvictions() > 0 && statistics.getEvictions() > evictionThreshold) {
            return new Status("WARN");
        }
        if (statistics.getPercentFree() > 0 && statistics.getPercentFree() < threshold) {
            return Status.OUT_OF_SERVICE;
        }
        return Status.UP;
    }
}
