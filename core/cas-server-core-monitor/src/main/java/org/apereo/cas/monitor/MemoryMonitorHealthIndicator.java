package org.apereo.cas.monitor;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import java.text.NumberFormat;

/**
 * Monitors JVM memory usage.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
@RequiredArgsConstructor
public class MemoryMonitorHealthIndicator extends AbstractHealthIndicator {

    private static final int PERCENTAGE_VALUE = 100;

    /**
     * Percent free memory below which a warning is reported.
     */
    private final double freeMemoryWarnThreshold;

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        val runtime = Runtime.getRuntime();
        val totalMemory = runtime.totalMemory();
        val freeMemory = runtime.freeMemory();
        val usedMemory = totalMemory - freeMemory;
        val maxMemory = runtime.maxMemory();
        val availableMemory = maxMemory - usedMemory;
        val availableMemoryPercentage = (double) availableMemory * PERCENTAGE_VALUE / maxMemory;
        val percentFormat = NumberFormat.getPercentInstance();
        builder
            .withDetail("availableMemory", availableMemory)
            .withDetail("maxMemory", maxMemory)
            .withDetail("usedMemory", usedMemory)
            .withDetail("totalMemory", totalMemory)
            .withDetail("freeMemory", freeMemory)
            .withDetail("freeMemoryPercentage", percentFormat.format(availableMemoryPercentage))
            .status(availableMemoryPercentage < freeMemoryWarnThreshold ? Status.DOWN : Status.UP);
    }
}
