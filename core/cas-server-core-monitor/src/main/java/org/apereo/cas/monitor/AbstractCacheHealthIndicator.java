package org.apereo.cas.monitor;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.monitor.MonitorWarningProperties;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract base class for monitors that observe cache storage systems.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@Slf4j
@AllArgsConstructor
public abstract class AbstractCacheHealthIndicator extends AbstractHealthIndicator {
    /**
     * CAS settings.
     */
    protected final CasConfigurationProperties casProperties;

    @Override
    protected void doHealthCheck(final Health.Builder builder) throws Exception {

        try {
            final CacheStatistics[] statistics = getStatistics();
            if (statistics == null || statistics.length == 0) {
                builder.outOfService().withDetail("message", "Cache statistics are not available.");
                return;
            }

            final Set<Status> statuses = Arrays.stream(statistics).map(this::status).collect(Collectors.toSet());
            if (statuses.contains(Status.OUT_OF_SERVICE)) {
                builder.outOfService();
            } else if (statuses.contains(Status.DOWN)) {
                builder.down();
            } else if (statuses.contains(new Status("WARN"))) {
                builder.status("WARN");
            } else {
                builder.up();
            }

            Arrays.stream(statistics).forEach(s ->
                builder.withDetail("size", s.getSize())
                    .withDetail("capacity", s.getCapacity())
                    .withDetail("evictions", s.getEvictions())
                    .withDetail("percentFree", s.getPercentFree())
                    .withDetail("name", s.getName()));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            builder.down(e);
        }

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
        final MonitorWarningProperties warn = casProperties.getMonitor().getWarn();
        if (statistics.getEvictions() > 0 && statistics.getEvictions() > warn.getEvictionThreshold()) {
            return new Status("WARN");
        }
        if (statistics.getPercentFree() > 0 && statistics.getPercentFree() < warn.getThreshold()) {
            return Status.OUT_OF_SERVICE;
        }
        return Status.UP;
    }
}
