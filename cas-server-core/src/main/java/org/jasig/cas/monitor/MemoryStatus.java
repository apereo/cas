/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.monitor;

/**
 * Describes the memory status of the JVM.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class MemoryStatus extends Status {

    private static final double BYTES_PER_MB = 1048510.0;

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
        super(code, String.format("%.2fMB free, %.2fMB total.", free / BYTES_PER_MB, total / BYTES_PER_MB));
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
