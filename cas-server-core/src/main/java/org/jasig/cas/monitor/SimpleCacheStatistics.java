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

import java.util.Formatter;

/**
 * Simple implementation of cache statistics.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public class SimpleCacheStatistics implements CacheStatistics {

    private static final double BYTES_PER_MB = 1048510.0;

    private final long size;

    private final long capacity;

    private final long evictions;

    private String name;


    /**
     * Creates a new instance with given parameters.
     *
     * @param size Current cache size (e.g. items, bytes, etc).
     * @param capacity Current cache capacity (e.g. items, bytes, etc).  The units of capacity must be equal to size
     *                 in order to produce a meaningful value for {@link #getPercentFree}.
     * @param evictions Number of evictions reported by cache.
     */
    public SimpleCacheStatistics(final long size, final long capacity, final long evictions) {
        this.size = size;
        this.capacity = capacity;
        this.evictions = evictions;
    }


    /**
     * Creates a new named instance with given parameters.
     *
     * @param size Current cache size (e.g. items, bytes, etc).
     * @param capacity Current cache capacity (e.g. items, bytes, etc).  The units of capacity must be equal to size
     *                 in order to produce a meaningful value for {@link #getPercentFree}.
     * @param evictions Number of evictions reported by cache.
     * @param name Name of cache instance to which statistics apply.
     */
    public SimpleCacheStatistics(final long size, final long capacity, final long evictions, final String name) {
        this.size = size;
        this.capacity = capacity;
        this.evictions = evictions;
        this.name = name;
    }

    public long getSize() {
        return this.size;
    }


    public long getCapacity() {
        return this.capacity;
    }


    public long getEvictions() {
        return this.evictions;
    }


    public int getPercentFree() {
        if (this.capacity == 0) {
            return 0;
        }
        return (int) ((this.capacity - this.size) * 100 / this.capacity);
    }


    public void toString(final StringBuilder builder) {
        if (this.name != null) {
            builder.append(this.name).append(':');
        }
        final Formatter formatter = new Formatter(builder);
        formatter.format("%.2f", this.size / BYTES_PER_MB);
        builder.append("MB used, ");
        builder.append(getPercentFree()).append("% free, ");
        builder.append(this.evictions).append(" evictions");
    }


    /**
     * Gets a descriptive name of the cache instance for which statistics apply.
     *
     * @return Name of cache instance/host to which statistics apply.
     */
    public String getName() {
        return this.name;
    }
}
