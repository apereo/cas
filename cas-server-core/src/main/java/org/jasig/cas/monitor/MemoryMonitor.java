/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.monitor;

/**
 * Monitors JVM memory usage.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class MemoryMonitor implements Monitor<MemoryStatus> {
   
    /** Default percent free memory warning threshold. */
    public static final int DEFAULT_FREE_MEMORY_WARN_THRESHOLD = 10;
   
    /** Percent free memory warning threshold. */
    private long freeMemoryWarnThreshold = DEFAULT_FREE_MEMORY_WARN_THRESHOLD;


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


    /** {@inheritDoc} */
    public String getName() {
        return MemoryMonitor.class.getSimpleName();
    }


    /** {@inheritDoc} */
    public MemoryStatus observe() {
        final StatusCode code;
        final long free = Runtime.getRuntime().freeMemory();
        final long total = Runtime.getRuntime().totalMemory();
        if (free * 100 / total < freeMemoryWarnThreshold) {
            code = StatusCode.WARN;
        } else {
            code = StatusCode.OK;
        }
        return new MemoryStatus(code, free, total);
    }
}
