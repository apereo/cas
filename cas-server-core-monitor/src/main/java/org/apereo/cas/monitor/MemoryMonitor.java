package org.apereo.cas.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Monitors JVM memory usage.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class MemoryMonitor implements Monitor<MemoryStatus> {

    /** Percent free memory warning threshold. */
    private long freeMemoryWarnThreshold;

    private static final int PERCENTAGE_VALUE = 100;

    /**
     * Sets the percent of free memory below which a warning is reported.
     *
     * @param threshold Percent free memory warning threshold.
     */
    public void setFreeMemoryWarnThreshold(final long threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Warning threshold must be non-negative.");
        }
        this.freeMemoryWarnThreshold = threshold;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return MemoryMonitor.class.getSimpleName();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MemoryStatus observe() {
        final StatusCode code;
        final long free = Runtime.getRuntime().freeMemory();
        final long total = Runtime.getRuntime().totalMemory();
        if (free * PERCENTAGE_VALUE / total < this.freeMemoryWarnThreshold) {
            code = StatusCode.WARN;
        } else {
            code = StatusCode.OK;
        }
        return new MemoryStatus(code, free, total);
    }
}
