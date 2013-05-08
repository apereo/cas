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
 * Monitors JVM memory usage.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
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
    @Override
    public String getName() {
        return MemoryMonitor.class.getSimpleName();
    }


    /** {@inheritDoc} */
    @Override
    public MemoryStatus observe() {
        final StatusCode code;
        final long free = Runtime.getRuntime().freeMemory();
        final long total = Runtime.getRuntime().totalMemory();
        if (free * 100 / total < this.freeMemoryWarnThreshold) {
            code = StatusCode.WARN;
        } else {
            code = StatusCode.OK;
        }
        return new MemoryStatus(code, free, total);
    }
}
