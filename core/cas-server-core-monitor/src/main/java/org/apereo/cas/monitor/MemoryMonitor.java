package org.apereo.cas.monitor;

/**
 * Monitors JVM memory usage.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class MemoryMonitor implements Monitor<MemoryStatus> {

    private static final int PERCENTAGE_VALUE = 100;

    /** Percent free memory below which a warning is reported. */
    private final long freeMemoryWarnThreshold;

    public MemoryMonitor(final long threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Warning threshold must be non-negative.");
        }
        this.freeMemoryWarnThreshold = threshold;
    }

    @Override
    public String getName() {
        return MemoryMonitor.class.getSimpleName();
    }

    @Override
    public MemoryStatus observe() {
        final StatusCode code;
        final long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        final long total = Runtime.getRuntime().maxMemory();
        final long free = total - used;
        if (free * PERCENTAGE_VALUE / total < this.freeMemoryWarnThreshold) {
            code = StatusCode.WARN;
        } else {
            code = StatusCode.OK;
        }
        return new MemoryStatus(code, free, total, used);
    }
}
