package org.apereo.cas.monitor;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * Monitors JVM memory usage.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
@RequiredArgsConstructor
public class MemoryMonitor extends AbstractHealthIndicator {

    private static final int PERCENTAGE_VALUE = 100;

    /**
     * Percent free memory below which a warning is reported.
     */
    private final long freeMemoryWarnThreshold;

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        val used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        val total = Runtime.getRuntime().maxMemory();
        val free = total - used;
        if (free * PERCENTAGE_VALUE / total < this.freeMemoryWarnThreshold) {
            buildHealthCheckStatus(builder.down(), free, total);
        } else {
            buildHealthCheckStatus(builder.up(), free, total);
        }
    }

    private void buildHealthCheckStatus(final Health.Builder builder,
                                        final long freeMemory, final long totalMemory) {
        builder
            .withDetail("freeMemory", freeMemory)
            .withDetail("totalMemory", totalMemory);
    }
}
