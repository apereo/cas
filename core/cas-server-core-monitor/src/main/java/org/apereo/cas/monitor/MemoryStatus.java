package org.apereo.cas.monitor;

/**
 * Describes the memory status of the JVM.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class MemoryStatus extends Status {

    private static final double BYTES_PER_MB = 1048510.0;
    private static final double PERCENTAGE = 100.0;

    /**
     * JVM free memory.
     */
    private final long freeMemory;

    /**
     * JVM total memory.
     */
    private final long totalMemory;

    /**
     * Creates a new status object with the given code.
     *
     * @param code  Status code.
     * @param free  JVM free memory in bytes.
     * @param total JVM total memory in bytes.
     * @param used  the used
     * @see #getCode() #getCode()
     */
    public MemoryStatus(final StatusCode code, final long free, final long total, final long used) {
        super(code, String.format("%.2fMB free (%.2f%%), %.2fMB used, %.2fMB total.", free / BYTES_PER_MB,
                free * PERCENTAGE / total,
                used / BYTES_PER_MB,
                total / BYTES_PER_MB));
        this.freeMemory = free;
        this.totalMemory = total;
    }


    /**
     * Gets JVM free memory.
     *
     * @return Free memory in bytes.
     */
    public long getFreeMemory() {
        return this.freeMemory;
    }

    /**
     * Gets JVM total memory.
     *
     * @return Max memory in bytes.
     */
    public long getTotalMemory() {
        return this.totalMemory;
    }
}
