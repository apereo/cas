/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.monitor;

/**
 * Describes the memory status of the JVM.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class MemoryStatus extends Status {
    /** JVM free memory. */
    private final long freeMemory;
    
    /** JVM total memory. */
    private final long totalMemory;


    /**
     * Creates a new status object with the given code.
     *
     * @param code Status code.
     * @param free JVM free memory in bytes.
     * @param total JVM total memory in bytes.
     *
     * @see #getCode()
     */
    public MemoryStatus(final StatusCode code, final long free, final long total) {
        super(code, String.format("%sM free memory available, %sM total.", free / 1048576, total / 1048576));
        this.freeMemory = free;
        this.totalMemory = total;
    }

    /**
     * Gets JVM free memory.
     *
     * @return Free memory in bytes.
     */
    public long getFreeMemory() {
        return freeMemory;
    }


    /**
     * Gets JVM total memory.
     *
     * @return Max memory in bytes.
     */
    public long getTotalMemory() {
        return totalMemory;
    }
}
