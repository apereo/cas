package org.jasig.cas.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Monitors JVM memory usage.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
@Component("memoryMonitor")
public class MemoryMonitor implements Monitor<MemoryStatus> {

    /** Default percent free memory warning threshold. */
    public static final int DEFAULT_FREE_MEMORY_WARN_THRESHOLD = 10;
    private static final int PERCENTAGE_VALUE = 100;

    /** Percent free memory warning threshold. */
    private long freeMemoryWarnThreshold = DEFAULT_FREE_MEMORY_WARN_THRESHOLD;


    /**
     * Sets the percent of free memory below which a warning is reported.
     *
     * @param threshold Percent free memory warning threshold.
     */
    @Autowired
    public void setFreeMemoryWarnThreshold(@Value("${cas.monitor.free.mem.threshold:10}")
                                               final long threshold) {
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
